package com.ohgiraffers.section01;

import com.ohgiraffers.section03.persistencecontext.Menu;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class A_EntityManagerLifeCycleTests {

    /* 필기.
     *  엔티티 매니저 팩토리(EntityManagerFactory)란?
     *   엔티티 매니저를 생성할 수 있는 기능을 제공하는 팩토리 클래스이다.
     *   thread-safe하기 때문에 여러 스레드가 동시에 접근해도 안전하므로 서로 다른 스레드 간 공유해서 재사용한다.
     *   thread-safe한 기능을 요청 스코프마다 생성하기에는 비용(시간, 메모리) 부담이 크므로
     *   application 스코프와 동일하게 싱글톤으로 생성해서 관리하는 것이 효율적이다.
     *   따라서 데이터베이스를 사용하는 애플리케이션 당 한 개의 EntityManagerFactory를 생성한다.
     *
     * 필기.
     *  엔티티 매니저(EntityManager)란?
     *   엔티티 매니저는 엔티티를 저장하는 메모리 상의 데이터베이스를 관리하는 인스턴스이다.
     *   엔티티를 저장하고, 수정, 삭제, 조회하는 등의 엔티티와 관련된 모든 일을 한다.
     *   엔티티 매니저는 thread-safe하지 않기 때문에 동시성 문제가 발생할 수 있다.
     *   따라서 스레드 간 공유를 하지 않고, web의 경우 일반적으로 request scope와 일치시킨다.
     *
     * 필기.
     *  영속성 컨텍스트(PersistenceContext)란?
     *   엔티티 매니저를 통해 엔티티를 저장하거나 조회하면 엔티티 매니저는 영속성 컨텍스트에 엔티티를 보관하고 관리한다.
     *   영속성 컨텍스트는 엔티티를 key-value 방식으로 저장하는 저장소이다.
     *   영속성 컨텍스트는 엔티티 매니저를 생성할 때 같이 하나 만들어진다.
     *   그리고 엔티티 매니저를 통해서 영속성 컨텍스트에 접근할 수 있고, 또 관리할 수 있다.
    * */
    private static EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    @BeforeAll
    public static void initFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("jpatest");
    }

    @BeforeEach
    public void initManager() {
        entityManager = entityManagerFactory.createEntityManager();
    }

    @Test
    public void 엔티티_매니저_팩토리와_엔티티_매니저_생명주기_확인1() {
        System.out.println("entityManagerFactory.hashCode: " + entityManagerFactory.hashCode());
        System.out.println("entityManager.hashCode: " + entityManager.hashCode());
    }

    @Test
    public void 엔티티_매니저_팩토리와_엔티티_매니저_생명주기_확인2() {
        System.out.println("entityManagerFactory.hashCode: " + entityManagerFactory.hashCode());
        System.out.println("entityManager.hashCode: " + entityManager.hashCode());
    }

    @AfterAll
    public static void closeFactory() {
        entityManagerFactory.close();
    }

    @AfterEach
    public void closeManager() {
        entityManager.close();
    }

    @Test
    public void 준영속성_detach_테스트(){
        Menu foundMenu1 = entityManager.find(Menu.class, 11);
        Menu foundMenu2 = entityManager.find(Menu.class, 12);

        /* 설명.
         *  영속성 컨텍스트가 관리하던 엔티티 객체를 관리하지 않는 상태가 되게 한 것을 준영속 상태래ㅏ고 한다.
         *  detach가 준영속 상태를 만들기 위한 메소드이다.
        **/
        entityManager.detach(foundMenu2);

        foundMenu1.setMenuPrice(5000);
        foundMenu2.setMenuPrice(5000);

        assertEquals(5000, entityManager.find(Menu.class, 11).getMenuPrice());
        assertEquals(5000, entityManager.find(Menu.class, 12).getMenuPrice());
    }

    @Test
    public void 준영속성_clear_close_테스트(){
        Menu foundMenu1 = entityManager.find(Menu.class, 11);
        Menu foundMenu2 = entityManager.find(Menu.class, 12);

        /* 설명. 영속성 컨텍스트로 관리되던 엔티티 객체들을 모두 비영속 상태로 바꿈 */
//        entityManager.clear();

        /* 설명. 영속성 컨텍스트 및 엔티티 매니저까지 종료해 버린다. (사용 불가) */
        entityManager.close();

        foundMenu1.setMenuPrice(5000);
        foundMenu2.setMenuPrice(5000);

        /* 설명. DB에서 새로 조회 해온 객체를 영속 상태로 두기 때문에 전혀 다른 결과가 나온다. */
        assertEquals(5000, entityManager.find(Menu.class, 11).getMenuPrice());
        assertEquals(5000, entityManager.find(Menu.class, 12).getMenuPrice());
    }

    @Test
    public void 병합_merge_수정_테스트(){
        Menu menuToDetach = entityManager.find(Menu.class, 2);
        entityManager.detach(menuToDetach);

        menuToDetach.setMenuName("수박죽");
        Menu refoundMenu = entityManager.find(Menu.class, 2);       // refoundMenu에는 기존의 메뉴 이름이 있다.

        System.out.println(menuToDetach.hashCode());
        System.out.println(refoundMenu.hashCode());

        entityManager.merge(menuToDetach);

        Menu mergedMenu = entityManager.find(Menu.class, 2);
        assertEquals("수박죽", mergedMenu.getMenuName());
    }

    @Test
    public void 병합_merge_삽입_테스트(){
        Menu menuToDetach = entityManager.find(Menu.class, 2);
        entityManager.detach(menuToDetach);

        menuToDetach.setMenuCode(999);
        menuToDetach.setMenuName("수박죽");

        entityManager.merge(menuToDetach);

        Menu newMenu = entityManager.find(Menu.class, 2);
        Menu mergedMenu = entityManager.find(Menu.class, 999);
        assertNotEquals(mergedMenu.getMenuCode(), newMenu.getMenuCode());
        
    }
}
