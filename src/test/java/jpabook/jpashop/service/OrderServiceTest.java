package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    //@Autowired MemberService memberService;
    //@Autowired ItemService itemService;

    @Test
    public void 상품주문(){
        Member member = createMember();

        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.",1,getOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다", 10000 * orderCount, getOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.",8,book.getStockQuantity());
    }

    @Test
    public void 주문취소(){
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        Long getOrder = orderService.order(member.getId(), book.getId(), 2);
        assertEquals("주문 성공해서 재고 수량이 줄어야 함",8,book.getStockQuantity());

        orderService.cancelOrder(getOrder);

        Order order = orderRepository.findOne(getOrder);

        assertEquals("주문 취소해서 재고 수량이 원복되어야 함",10,book.getStockQuantity());
        assertEquals("주문 취소시 상태는 CANCEL이다.", OrderStatus.CANCEL, order.getStatus());
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과(){
        Member member = createMember();

        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        orderService.order(member.getId(), book.getId(), orderCount);

        fail("재고 수량 부족 예외가 발생해야 한다.");
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울","강가","123-123"));
        em.persist(member);
        return member;
    }
}