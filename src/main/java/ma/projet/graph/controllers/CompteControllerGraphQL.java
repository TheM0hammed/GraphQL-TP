package ma.projet.graph.controllers;

import lombok.AllArgsConstructor;
import ma.projet.graph.entities.*;
import ma.projet.graph.repositories.CompteRepository;
import ma.projet.graph.repositories.TransactionRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class CompteControllerGraphQL {

    private CompteRepository compteRepository;
    private TransactionRepository transactionRepository;
    @QueryMapping
    public List<Compte> allComptes(){
        return compteRepository.findAll();
    }

    @QueryMapping
    public List<Compte> findByType(@Argument TypeCompte type){
        return compteRepository.findByType(type);
    }

    @QueryMapping
    public Compte compteById(@Argument Long id){
        Compte compte =  compteRepository.findById(id).orElse(null);
        if(compte == null) throw new RuntimeException(String.format("Compte %s not found", id));
        else return compte;
    }

    @MutationMapping
    public Compte saveCompte(@Argument Compte compte){
       return compteRepository.save(compte);
    }

    @MutationMapping
    public String deleteCompte(@Argument Long id) {
        if (compteRepository.existsById(id)) {
            compteRepository.deleteById(id);
            return String.format("Compte %s deleted successfully", id);
        } else {
            throw new RuntimeException(String.format("Compte %s not found", id));
        }
    }
    @QueryMapping
    public Map<String, Object> totalSolde() {
        long count = compteRepository.count(); // Nombre total de comptes
        double sum = compteRepository.sumSoldes(); // Somme totale des soldes
        double average = count > 0 ? sum / count : 0; // Moyenne des soldes

        return Map.of(
                "count", count,
                "sum", sum,
                "average", average
        );
    }
    @QueryMapping
    public List<Transaction> transactionsByCompte(@Argument Long compteId) {
        return transactionRepository.findByCompteId(compteId);
    }

    @MutationMapping
    public Transaction addTransaction(@Argument TransactionRequest transactionRequest) {
        Long compteId = transactionRequest.getCompteId();
        double amount = transactionRequest.getAmount();
        TransactionType type = transactionRequest.getType();

        Compte compte = compteRepository.findById(compteId).orElseThrow(() ->
                new RuntimeException(String.format("Compte %s not found", compteId))
        );

        if (type == TransactionType.WITHDRAWAL && compte.getSolde() < amount) {
            throw new RuntimeException("Insufficient balance for withdrawal");
        }

        // Update the compte balance
        double newSolde = type == TransactionType.DEPOSIT ? compte.getSolde() + amount : compte.getSolde() - amount;
        compte.setSolde(newSolde);
        compteRepository.save(compte);

        // Create and save the transaction
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());
        transaction.setType(type);
        transaction.setCompte(compte);

        return transactionRepository.save(transaction);
    }
    @QueryMapping
    public Map<String, Object> transactionStats() {
        long count = transactionRepository.count(); // Total number of transactions
        double sumDepot = transactionRepository.sumDepot() != null ? transactionRepository.sumDepot() : 0; // Sum of deposits
        double sumRetrait = transactionRepository.sumRetrait() != null ? transactionRepository.sumRetrait() : 0; // Sum of withdrawals

        return Map.of(
                "count", count,
                "sumDepot", sumDepot,
                "sumRetrait", sumRetrait
        );
    }
}
