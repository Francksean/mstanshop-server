#!/usr/bin/env node
/**
 * Seed script — insère categories.json / fournisseurs.json / produits.json en base via l'API REST.
 * Usage : node datas-mstanshop/seed.js --url=https://api.example.com --email=admin@x.com --password=xxxxx
 */

const fs = require("fs");
const path = require("path");

const DATA_DIR = __dirname;

function parseArgs() {
    const args = {};
    for (const arg of process.argv.slice(2)) {
        const match = arg.match(/^--([^=]+)=(.*)$/);
        if (match) {
            args[match[1]] = match[2];
        }
    }
    const missing = ["url", "email", "password"].filter((key) => !args[key]);
    if (missing.length > 0) {
        console.error(`Arguments manquants : ${missing.map((m) => "--" + m).join(", ")}`);
        console.error("Usage : node seed.js --url=https://api.example.com --email=admin@x.com --password=xxxxx");
        process.exit(1);
    }
    return { baseUrl: args.url.replace(/\/$/, ""), email: args.email, password: args.password };
}

function contentTypeFor(filePath) {
    const ext = path.extname(filePath).toLowerCase();
    switch (ext) {
        case ".jpeg":
        case ".jpg":
            return "image/jpeg";
        case ".png":
            return "image/png";
        case ".webp":
            return "image/webp";
        case ".gif":
            return "image/gif";
        default:
            throw new Error(`Extension d'image non supportée : ${ext}`);
    }
}

class ApiClient {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
        this.accessToken = null;
    }

    async login(email, password) {
        const res = await fetch(`${this.baseUrl}/api/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
        });
        if (!res.ok) {
            throw new Error(`Échec du login (${res.status}) : ${await res.text()}`);
        }
        const data = await res.json();
        this.accessToken = data.accessToken;
    }

    async request(method, urlPath, body) {
        const res = await fetch(`${this.baseUrl}${urlPath}`, {
            method,
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${this.accessToken}`,
            },
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
        const text = await res.text();
        let json = null;
        if (text) {
            try {
                json = JSON.parse(text);
            } catch {
                json = text;
            }
        }
        if (!res.ok) {
            const err = new Error(`${method} ${urlPath} a échoué (${res.status}) : ${text}`);
            err.status = res.status;
            throw err;
        }
        return json;
    }

    async uploadImage(localPath, scope, entityId) {
        const contentType = contentTypeFor(localPath);
        const fileName = path.basename(localPath);
        const presign = await this.request("POST", "/api/admin/uploads/presign", {
            scope,
            entityId,
            fileName,
            contentType,
        });

        const bytes = fs.readFileSync(localPath);
        const putRes = await fetch(presign.uploadUrl, {
            method: "PUT",
            headers: { "Content-Type": contentType },
            body: bytes,
        });
        if (!putRes.ok) {
            throw new Error(`Échec de l'upload vers R2 pour ${localPath} (${putRes.status})`);
        }

        return presign.objectKey;
    }
}

async function seedCategories(api, categories, resultCounts) {
    const categoryIdByName = new Map();

    for (const category of categories) {
        console.log(`Création catégorie : ${category.name}`);
        let created;
        try {
            created = await api.request("POST", "/api/admin/categories", {
                name: category.name,
                description: category.description,
                bannerObjectKey: null,
                thumbnailObjectKey: null,
                parentId: null,
            });
            resultCounts.categories.ok++;
        } catch (e) {
            console.error(`  Échec création catégorie "${category.name}" : ${e.message}`);
            resultCounts.categories.failed.push({ name: category.name, error: e.message });
            continue;
        }
        categoryIdByName.set(category.name, created.id);

        if (category.bannerImagePath) {
            try {
                const bannerPath = path.resolve(DATA_DIR, category.bannerImagePath);
                const objectKey = await api.uploadImage(bannerPath, "CATEGORY_BANNER", created.id);
                await api.request("POST", `/api/admin/categories/${created.id}/banner/confirm`, { objectKey });
                console.log(`  Bannière uploadée pour "${category.name}"`);
            } catch (e) {
                console.error(`  Échec upload bannière pour "${category.name}" : ${e.message}`);
            }
        }

        for (const sub of category.subCategories || []) {
            console.log(`  Création sous-catégorie : ${sub.name}`);
            let createdSub;
            try {
                createdSub = await api.request("POST", "/api/admin/categories", {
                    name: sub.name,
                    description: sub.description,
                    bannerObjectKey: null,
                    thumbnailObjectKey: null,
                    parentId: created.id,
                });
                resultCounts.categories.ok++;
            } catch (e) {
                console.error(`    Échec création sous-catégorie "${sub.name}" : ${e.message}`);
                resultCounts.categories.failed.push({ name: sub.name, error: e.message });
                continue;
            }
            categoryIdByName.set(sub.name, createdSub.id);

            if (sub.bannerImagePath) {
                try {
                    const bannerPath = path.resolve(DATA_DIR, sub.bannerImagePath);
                    const objectKey = await api.uploadImage(bannerPath, "CATEGORY_BANNER", createdSub.id);
                    await api.request("POST", `/api/admin/categories/${createdSub.id}/banner/confirm`, { objectKey });
                    console.log(`    Bannière uploadée pour "${sub.name}"`);
                } catch (e) {
                    console.error(`    Échec upload bannière pour "${sub.name}" : ${e.message}`);
                }
            }
        }
    }

    return categoryIdByName;
}

async function seedSuppliers(api, suppliers, resultCounts) {
    const supplierIdByName = new Map();

    for (const supplier of suppliers) {
        console.log(`Création fournisseur : ${supplier.nom}`);
        try {
            const created = await api.request("POST", "/api/admin/suppliers", {
                name: supplier.nom,
                primaryPhone: (supplier.contact || "").trim(),
                secondaryPhone: null,
                location: (supplier.localisation || "").trim() || null,
            });
            supplierIdByName.set(supplier.nom, created.id);
            resultCounts.suppliers.ok++;
        } catch (e) {
            console.error(`  Échec création fournisseur "${supplier.nom}" : ${e.message}`);
            resultCounts.suppliers.failed.push({ name: supplier.nom, error: e.message });
        }
    }

    return supplierIdByName;
}

async function seedProducts(api, products, categoryIdByName, supplierIdByName, resultCounts) {
    for (const product of products) {
        console.log(`Création produit : ${product.name}`);

        const categoryId = categoryIdByName.get(product.category);
        if (!categoryId) {
            console.error(`  Catégorie introuvable : "${product.category}" — produit ignoré`);
            resultCounts.products.failed.push({ name: product.name, error: `Catégorie introuvable : ${product.category}` });
            continue;
        }

        const supplierId = supplierIdByName.get(product.supplier);
        if (!supplierId) {
            console.error(`  Fournisseur introuvable : "${product.supplier}" — produit ignoré`);
            resultCounts.products.failed.push({ name: product.name, error: `Fournisseur introuvable : ${product.supplier}` });
            continue;
        }

        const variants = (product.variants || []).map((v) => ({
            colorName: v.colorName ?? null,
            colorHex: v.colorHex ?? null,
            sizes: v.size,
            stock: product.stock,
            active: true,
        }));

        let created;
        try {
            created = await api.request("POST", "/api/admin/products", {
                name: product.name,
                description: product.description,
                price: product.price,
                purchasePrice: product.purchasedPrice ?? null,
                stock: product.stock,
                categoryId,
                supplierId,
                images: [],
                variants,
            });
            resultCounts.products.ok++;
        } catch (e) {
            console.error(`  Échec création produit "${product.name}" : ${e.message}`);
            resultCounts.products.failed.push({ name: product.name, error: e.message });
            continue;
        }

        for (const img of product.images || []) {
            try {
                const imagePath = path.resolve(DATA_DIR, img.image);
                const objectKey = await api.uploadImage(imagePath, "PRODUCT_IMAGE", created.id);
                await api.request("POST", `/api/admin/products/${created.id}/images/confirm`, {
                    objectKey,
                    markAsThumbnail: !!img.markAsThumbnail,
                    variantId: null,
                });
                resultCounts.images.ok++;
            } catch (e) {
                console.error(`  Échec upload image "${img.image}" pour "${product.name}" : ${e.message}`);
                resultCounts.images.failed.push({ name: `${product.name} / ${img.image}`, error: e.message });
            }
        }
    }
}

async function main() {
    const { baseUrl, email, password } = parseArgs();

    const categories = JSON.parse(fs.readFileSync(path.join(DATA_DIR, "categories.json"), "utf-8"));
    const suppliers = JSON.parse(fs.readFileSync(path.join(DATA_DIR, "fournisseurs.json"), "utf-8"));
    const products = JSON.parse(fs.readFileSync(path.join(DATA_DIR, "produits.json"), "utf-8"));

    const api = new ApiClient(baseUrl);
    console.log(`Connexion à ${baseUrl}...`);
    await api.login(email, password);
    console.log("Connecté.\n");

    const resultCounts = {
        categories: { ok: 0, failed: [] },
        suppliers: { ok: 0, failed: [] },
        products: { ok: 0, failed: [] },
        images: { ok: 0, failed: [] },
    };

    console.log("=== Catégories ===");
    const categoryIdByName = await seedCategories(api, categories, resultCounts);

    console.log("\n=== Fournisseurs ===");
    const supplierIdByName = await seedSuppliers(api, suppliers, resultCounts);

    console.log("\n=== Produits ===");
    await seedProducts(api, products, categoryIdByName, supplierIdByName, resultCounts);

    console.log("\n=== Résumé ===");
    console.log(`Catégories : ${resultCounts.categories.ok} créées, ${resultCounts.categories.failed.length} échecs`);
    console.log(`Fournisseurs : ${resultCounts.suppliers.ok} créés, ${resultCounts.suppliers.failed.length} échecs`);
    console.log(`Produits : ${resultCounts.products.ok} créés, ${resultCounts.products.failed.length} échecs`);
    console.log(`Images : ${resultCounts.images.ok} uploadées, ${resultCounts.images.failed.length} échecs`);

    for (const key of ["categories", "suppliers", "products", "images"]) {
        if (resultCounts[key].failed.length > 0) {
            console.log(`\nÉchecs (${key}) :`);
            for (const f of resultCounts[key].failed) {
                console.log(`  - ${f.name} : ${f.error}`);
            }
        }
    }
}

main().catch((e) => {
    console.error("Erreur fatale :", e);
    process.exit(1);
});
