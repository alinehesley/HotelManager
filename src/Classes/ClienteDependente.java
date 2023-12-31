package Classes;

import Classes.exceptions.CPFInvalidoException;
import Classes.exceptions.ClienteException;

import java.time.LocalDate;
import java.util.Objects;

public class ClienteDependente extends Cliente {
    private ClienteTitular titular;

    ClienteDependente(String nome, LocalDate dataNascimento, String cpf, ClienteTitular titular) throws ClienteException {
        super(nome, dataNascimento, cpf);

        this.titular = Objects.requireNonNull(titular);
        titular.addDependente(this);
    }

    ClienteDependente(String nome, LocalDate dataNascimento, String cpf, ClienteTitular titular, double conta) throws ClienteException {
        super(nome, dataNascimento, cpf, conta);

        this.titular = Objects.requireNonNull(titular);
        titular.addDependente(this);
    }
    ClienteDependente(String nome, LocalDate dataNascimento, String cpf, double conta) throws ClienteException {
        super(nome, dataNascimento, cpf, conta);

    }


    public ClienteTitular getTitular() {
        return titular;
    }

    @Override
    public Quarto getQuarto() {
        if (titular == null)
            return null;

        return titular.getQuarto();
    }
}
