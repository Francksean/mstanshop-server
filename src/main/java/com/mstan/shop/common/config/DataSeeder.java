package com.mstan.shop.common.config;

import com.mstan.shop.category.entity.Category;
import com.mstan.shop.category.repository.CategoryRepository;
import com.mstan.shop.common.util.SlugUtil;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.repository.ProductRepository;
import com.mstan.shop.user.entity.AuthProvider;
import com.mstan.shop.user.entity.Role;
import com.mstan.shop.user.entity.RoleName;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.repository.RoleRepository;
import com.mstan.shop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@mstan-shop.com}")
    private String adminEmail;

    @Value("${app.seed.admin-password:Admin123!}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));

        seedAdmin(adminRole);
        Category vetements = seedCategory("Vêtements", "Prêt-à-porter homme et femme");
        Category bijoux = seedCategory("Bijoux", "Colliers, bagues, bracelets et boucles d'oreilles");
        Category accessoires = seedCategory("Accessoires", "Sacs, ceintures, foulards et autres accessoires de mode");

        seedProduct("Robe en lin", "Robe légère en lin naturel, coupe fluide, idéale pour l'été", new BigDecimal("59.90"), 30, vetements);
        seedProduct("Chemise oversize", "Chemise coton oversize à motifs, unisexe", new BigDecimal("39.90"), 45, vetements);
        seedProduct("Collier en argent", "Collier fin en argent 925 avec pendentif minimaliste", new BigDecimal("34.90"), 20, bijoux);
        seedProduct("Bague dorée sertie", "Bague plaquée or serti de zircons", new BigDecimal("24.90"), 35, bijoux);
        seedProduct("Sac à main en cuir", "Sac à main en cuir véritable, finitions artisanales", new BigDecimal("89.90"), 15, accessoires);
        seedProduct("Écharpe en cachemire", "Écharpe douce en mélange de cachemire, plusieurs coloris", new BigDecimal("44.90"), 25, accessoires);

        log.info("Compte USER par défaut disponible : {}", userRole.getName());
    }

    private void seedAdmin(Role adminRole) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFirstName("Admin");
        admin.setLastName("MSTAN");
        admin.setAuthProvider(AuthProvider.LOCAL);
        admin.setEnabled(true);
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);

        log.info("Compte admin créé : {} / {}", adminEmail, adminPassword);
    }

    private Category seedCategory(String name, String description) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    category.setDescription(description);
                    category.setSlug(SlugUtil.slugify(name));
                    return categoryRepository.save(category);
                });
    }

    private void seedProduct(String name, String description, BigDecimal price, int stock, Category category) {
        if (productRepository.existsByNameIgnoreCase(name)) {
            return;
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setSlug(SlugUtil.slugify(name));
        product.setActive(true);
        productRepository.save(product);
    }
}
