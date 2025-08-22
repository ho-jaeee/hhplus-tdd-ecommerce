package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.infrastructure.dto.ProductIdName;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final SpringDataProductRepository jpaRepository;

    public ProductRepositoryImpl(SpringDataProductRepository jpaRepository) {

        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ProductJPA> findById(long id) {

        return jpaRepository.findById(id);
    }

    @Override
    public ProductJPA save(ProductJPA product) {

        return jpaRepository.save(product);
    }

    @Override
    public List<ProductJPA> findAll() {

        return jpaRepository.findAll();
    }

    @Override
    public Map<Long, String> findNameByProductId(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1) 인터페이스 프로젝션으로 (id, name) 리스트 조회
        List<ProductIdName> rows = jpaRepository.findByProductIdIn(productIds);

        // 2) id -> name 기본 맵 (중복 id는 마지막 값이 우선)
        Map<Long, String> idNameMap = rows.stream()
                .filter(r -> r.getproductId() != null)
                .collect(Collectors.toMap(
                        ProductIdName::getproductId,
                        ProductIdName::getname,
                        (a, b) -> b // merge: 뒤에 온 값 우선
                ));

        // 3) 입력 순서 보존용 LinkedHashMap 으로 재정렬 (없으면 null 허용/필요 시 제거)
        Map<Long, String> p = new LinkedHashMap<>(productIds.size());
        for (Long pid : productIds) {
            p.put(pid, idNameMap.get(pid)); // 존재하지 않는 id는 null
        }
        return p;
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
