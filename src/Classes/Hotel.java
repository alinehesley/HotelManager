package Classes;


import Classes.exceptions.*;

import java.time.LocalDate;
import java.util.ArrayList;

public class Hotel {
    private ArrayList<Quarto> listaQuartos;
    private ArrayList<Cliente> listaClientes;

    private String nome;
    private String endereco;
    private String telefone;
    private String categoria;
    private int totalVagas;

    public Hotel(String nome, String endereco, String telefone, String categoria) {
        this.nome = nome;
        this.endereco = endereco;
        this.telefone = telefone;
        this.categoria = categoria;
        this.listaQuartos = new ArrayList<Quarto>();
        this.listaClientes = new ArrayList<Cliente>();
    }

    // Cria uma cópia da lista de quartos
    public ArrayList<Quarto> getListaQuartos() {
        return new ArrayList<>(listaQuartos);
    }

    // Cria uma cópia da lista de clientes
    public ArrayList<Cliente> getListaClientes() {
        return new ArrayList<>(listaClientes);
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getTotalVagas() {
        return totalVagas;
    }

    public void addQuarto(Quarto quarto) {
        listaQuartos.add(quarto);
    }

    public void addQuarto(int totalCamaCasal, int totalCamaSolteiro) {
        // TODO(thiago): Conferir se o número do quarto está correto
        listaQuartos.add(new Quarto(listaQuartos.size() + 1, totalCamaCasal, totalCamaSolteiro));
    }

    public void addQuarto(int numero, int totalCamaCasal, int totalCamaSolteiro) {
        listaQuartos.add(new Quarto(numero, totalCamaCasal, totalCamaSolteiro));
    }

    private boolean cpfJaCadastrado(String cpf) {
        for (Cliente c : listaClientes) {
            if (c.getCpf().equals(cpf)) {
                return true;
            }
        }
        return false;
    }

    public void cadastrarClienteTitular(ClienteTitular cliente) throws ClienteCadastradoException {
        if (cpfJaCadastrado(cliente.getCpf()))
            throw new ClienteCadastradoException("Cliente já cadastrado");

        listaClientes.add(cliente);

        for (ClienteDependente clienteDependente : cliente.getListaDependentes()) {
            cadastrarClienteDependente(clienteDependente);
        }
    }

    public void cadastrarClienteTitular(String nome, String cpf, LocalDate dataNascimento) throws ClienteCadastradoException, CPFInvalidoException {
        cadastrarClienteTitular(new ClienteTitular(nome, dataNascimento, cpf));
    }

    public void cadastrarClienteDependente(String nome, String cpf, LocalDate dataNascimento, String cpfTitular) throws ClienteException {
        Cliente titular = getClienteFromCpf(cpfTitular);
        if (titular == null)
            throw new ClienteException("Cliente titular não encontrado");
        if (!(titular instanceof ClienteTitular))
            throw new ClienteException("Cliente titular não é titular");

        cadastrarClienteDependente(new ClienteDependente(nome, dataNascimento, cpf, (ClienteTitular) titular));
    }

    public void cadastrarClienteDependente(ClienteDependente clienteDependente) throws ClienteCadastradoException {
        if (cpfJaCadastrado(clienteDependente.getCpf()))
            throw new ClienteCadastradoException();

        listaClientes.add(clienteDependente);
    }

    public Cliente getClienteFromCpf(String cpf) {
        for (Cliente c : listaClientes) {
            if (c.getCpf().equals(cpf)) {
                return c;
            }
        }

        return null;
    }

    public Quarto reservarQuarto(String cpf, int totalCamaCasal, int totalCamaSolteiro, LocalDate dataEntrada, LocalDate dataSaida) throws ClienteException {
        Cliente cliente = getClienteFromCpf(cpf);
        if (cliente == null)
            throw new ClienteException("Cliente não encontrado");
        if (!(cliente instanceof ClienteTitular))
            throw new ClienteException("Cliente não é titular");

        ClienteTitular clienteTitular = (ClienteTitular) cliente;
        if (clienteTitular.getTotalConta() >= 0.01)
            throw new ClienteException("Cliente possui débito de R$ %.2f e não pode reservar quarto", cliente.getConta());

        for (Quarto quarto : listaQuartos) {
            if (quarto.getTotalCamaCasal() == totalCamaCasal && quarto.getTotalCamaSolteiro() == totalCamaSolteiro) {
                try {
                    reservarQuarto(clienteTitular, quarto, dataEntrada, dataSaida);
                    return quarto;
                } catch (QuartoIndisponivelException e) {
                    // Ignora
                }
            }
        }

        // Retorna null caso não encontre um quarto disponível
        return null;
    }

    public void reservarQuarto(ClienteTitular cliente, Quarto quarto, LocalDate dataEntrada, LocalDate dataSaida) throws QuartoIndisponivelException, ClienteException {
        if (cliente.getConta() >= 0.01)
            throw new ClienteException("Cliente possui débito de R$ %.2f e não pode reservar quarto", cliente.getConta());
        quarto.fazerCheckIn(cliente, dataEntrada, dataSaida);
    }

    public Quarto getQuarto(int totalCamaCasal, int totalCamaSolteiro) {
        for (Quarto quarto : listaQuartos) {
            if (quarto.getTotalCamaCasal() == totalCamaCasal && quarto.getTotalCamaSolteiro() == totalCamaSolteiro) {
                return quarto;
            }
        }
        return null;
    }

    public void encerrarReserva(Quarto quarto, double desconto) throws QuartoNaoLocadoException {
        if (quarto.ehDisponivel())
            throw new QuartoNaoLocadoException(quarto.getNumero());

        // Inicia calculando o valor da conta
        double valorEstadia = quarto.calculaPrecoEstadia() - desconto;

        if (valorEstadia < 0.01) {
            valorEstadia = 0.0;
        }

        quarto.getTitular().addConta(valorEstadia);

        // Reinicializando o quarto
        quarto.fazerCheckOut();
    }

    // TODO(thiago): Realmente não precisa dessa função?
//    private boolean recebePagamento(Quarto quarto, boolean ehPago, double totalConsumo) {
//        if (ehPago) {
//            quarto.getTitular().setEhInadimplente(ehPago);
//            return true;
//        } else {
//            quarto.getTitular().setEhInadimplente(ehPago);
//            return false;
//        }
//    }

    // Calcula a capacidade de vagas no quarto
    // Chamado após um checkin ou um checkout
    public void calculaTotalVagas() {
        int capacidade = 0;
        for (Quarto quarto : listaQuartos) {
            if (quarto.ehDisponivel()) {
                capacidade += 2 * quarto.getTotalCamaCasal() + quarto.getTotalCamaCasal();
            }
        }
        this.totalVagas = capacidade;
    }

//    public boolean trocaClienteQuarto(Quarto quartoAtual, Quarto novoQuarto) {
//        // Transfere todos os clientes caso os quartos tenha camas o suficiente
//        if (!novoQuarto.ehDisponivel()) {
//            return false;
//        } else if (quartoAtual.getTotalCamaCasal() > novoQuarto.getTotalCamaCasal() | quartoAtual.getTotalCamaSolteiro() > novoQuarto.getTotalCamaSolteiro()) {
//            return false;
//        } else {
//            if (reservarQuarto(quartoAtual.getTitular(), novoQuarto, quartoAtual.getDataEntrada(), quartoAtual.getDataSaida())) {
//                quartoAtual.getTitular().setQuarto(novoQuarto);
//                quartoAtual.fazerCheckOut();
//                return true;
//
//            } else {
//                return false;
//            }
//        }
//
//    }
//
//    public boolean trocaClienteQuarto(Quarto quartoAtual, Quarto novoQuarto, Cliente novoTitular, ArrayList<Cliente> Novosdependentes) {
//        // Transfere todos os clientes caso os quartos tenha camas o suficiente
//        if (novoTitular instanceof ClienteDependente) {
//            ClienteTitular titular = new ClienteTitular(novoTitular.getNome(), novoTitular.getDataNascimento(), novoTitular.getCpf());
//
//
//        }
//
//    }


}
