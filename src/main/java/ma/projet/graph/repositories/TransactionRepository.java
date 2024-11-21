package ma.projet.graph.repositories;

import ma.projet.graph.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCompteId(Long compteId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'DEPOSIT'")
    Double sumDepot();

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'WITHDRAWAL'")
    Double sumRetrait();

}
