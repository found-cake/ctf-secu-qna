package kr.foundcake.secu_qna.repository;

import kr.foundcake.secu_qna.entity.Ticket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByOwnerUsernameOrderByCreatedAtDesc(String username);
    Optional<Ticket> findByIdAndOwnerUsername(Long id, String username);

    @Modifying
    @Query("delete from Ticket t where t.owner.id in :ownerIds")
    int deleteByOwnerIds(@Param("ownerIds") List<Long> ownerIds);
}
