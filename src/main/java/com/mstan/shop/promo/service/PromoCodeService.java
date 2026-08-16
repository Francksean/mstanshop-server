package com.mstan.shop.promo.service;

import com.mstan.shop.common.exception.DuplicateResourceException;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.promo.dto.PromoCodeRequest;
import com.mstan.shop.promo.dto.PromoCodeResponse;
import com.mstan.shop.promo.entity.PromoCode;
import com.mstan.shop.promo.entity.PromoDiscountType;
import com.mstan.shop.promo.mapper.PromoCodeMapper;
import com.mstan.shop.promo.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;

    @Transactional(readOnly = true)
    public List<PromoCodeResponse> getAll() {
        return promoCodeRepository.findAll().stream().map(promoCodeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PromoCodeResponse getById(UUID id) {
        return promoCodeMapper.toResponse(findPromoCode(id));
    }

    @Transactional
    public PromoCodeResponse create(PromoCodeRequest request) {
        if (promoCodeRepository.existsByCodeIgnoreCase(request.code())) {
            throw new DuplicateResourceException("Un code promo avec ce code existe déjà");
        }
        validatePercentageBound(request);

        PromoCode promoCode = new PromoCode();
        promoCode.setCode(request.code());
        promoCode.setDiscountType(request.discountType());
        promoCode.setDiscountValue(request.discountValue());
        promoCode.setExpiresAt(request.expiresAt());
        promoCode.setMinOrderAmount(request.minOrderAmount());
        promoCode.setMaxUses(request.maxUses());
        promoCode.setActive(request.active());
        promoCodeRepository.save(promoCode);

        return promoCodeMapper.toResponse(promoCode);
    }

    @Transactional
    public PromoCodeResponse update(UUID id, PromoCodeRequest request) {
        PromoCode promoCode = findPromoCode(id);

        if (!promoCode.getCode().equalsIgnoreCase(request.code())
                && promoCodeRepository.existsByCodeIgnoreCase(request.code())) {
            throw new DuplicateResourceException("Un code promo avec ce code existe déjà");
        }
        validatePercentageBound(request);

        promoCode.setCode(request.code());
        promoCode.setDiscountType(request.discountType());
        promoCode.setDiscountValue(request.discountValue());
        promoCode.setExpiresAt(request.expiresAt());
        promoCode.setMinOrderAmount(request.minOrderAmount());
        promoCode.setMaxUses(request.maxUses());
        promoCode.setActive(request.active());
        promoCodeRepository.save(promoCode);

        return promoCodeMapper.toResponse(promoCode);
    }

    @Transactional
    public void delete(UUID id) {
        PromoCode promoCode = findPromoCode(id);
        promoCodeRepository.delete(promoCode);
    }

    @Transactional(readOnly = true)
    public PromoCodeEvaluation evaluate(String code, BigDecimal subtotal) {
        Optional<PromoCode> found = promoCodeRepository.findByCodeIgnoreCase(code);
        if (found.isEmpty()) {
            return new PromoCodeEvaluation(false, "Ce code promo n'existe pas", null, BigDecimal.ZERO);
        }
        PromoCode promoCode = found.get();

        if (!promoCode.isActive()) {
            return new PromoCodeEvaluation(false, "Ce code promo n'est plus actif", promoCode, BigDecimal.ZERO);
        }
        if (promoCode.getExpiresAt() != null && promoCode.getExpiresAt().isBefore(Instant.now())) {
            return new PromoCodeEvaluation(false, "Ce code promo a expiré", promoCode, BigDecimal.ZERO);
        }
        if (promoCode.getMinOrderAmount() != null && subtotal.compareTo(promoCode.getMinOrderAmount()) < 0) {
            return new PromoCodeEvaluation(false,
                    "Le montant minimum de commande pour ce code promo est de " + promoCode.getMinOrderAmount() + " €",
                    promoCode, BigDecimal.ZERO);
        }
        if (promoCode.getMaxUses() != null && promoCode.getUsesCount() >= promoCode.getMaxUses()) {
            return new PromoCodeEvaluation(false, "Ce code promo a atteint sa limite d'utilisations", promoCode, BigDecimal.ZERO);
        }

        return new PromoCodeEvaluation(true, null, promoCode, computeDiscount(promoCode, subtotal));
    }

    private BigDecimal computeDiscount(PromoCode promoCode, BigDecimal subtotal) {
        if (promoCode.getDiscountType() == PromoDiscountType.PERCENTAGE) {
            return subtotal.multiply(promoCode.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return promoCode.getDiscountValue().min(subtotal);
    }

    private void validatePercentageBound(PromoCodeRequest request) {
        if (request.discountType() == PromoDiscountType.PERCENTAGE
                && request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Une remise en pourcentage ne peut pas dépasser 100");
        }
    }

    private PromoCode findPromoCode(UUID id) {
        return promoCodeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Code promo", id));
    }
}
