package ma.projet.graph.entities;

import lombok.Data;


@Data
public class TransactionRequest {
    private Long compteId;
    private double amount;
    private TransactionType type;
}

