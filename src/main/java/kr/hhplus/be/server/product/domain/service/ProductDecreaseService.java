package kr.hhplus.be.server.product.domain.service;


import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductDecreaseService {

    private final ProductRepository productRepository;

    @Transactional
    public void decreaseStock(long productId, int amount) {
        ProductJPA product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        product.decreaseQuantity(amount);
    }

}
