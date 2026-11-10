package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.ArquivoPersistenciaCorrompidoException;
import br.unicamp.ic.mc322.exception.ArquivoPersistenciaInexistenteException;
import br.unicamp.ic.mc322.exception.ArquivoPersistenciaVazioException;
import br.unicamp.ic.mc322.exception.DadosPersistidosInvalidosException;
import br.unicamp.ic.mc322.exception.FalhaEscritaPersistenciaException;
import br.unicamp.ic.mc322.exception.FalhaLeituraPersistenciaException;
import br.unicamp.ic.mc322.service.Planta;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Salva e carrega o estado da fábrica num arquivo XML. É a única classe que mexe no arquivo, a montagem do
 * XML fica no {@link PlantaXmlEscritor} e a leitura/validação no {@link PlantaXmlLeitor}.
 * <ul>
 *     <li>Salvar é atômico: grava num arquivo temporário e só depois troca pelo antigo, então uma falha no
 *     meio nunca estraga o save anterior;</li>
 *     <li>Carregar devolve uma fábrica nova e não mexe na que está em uso: quem decide trocar é o menu;</li>
 * </ul>
 *
 * @author Heitor Almeida
 */
public class PersistenciaPlanta {
    /** Onde o programa guarda o save, relativo à pasta em que ele roda */
    public static final Path CAMINHO_PADRAO = Path.of("dados", "planta.xml");

    /** Primeira linha do arquivo */
    private static final String DECLARACAO_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private final Path arquivo;

    /** Usa o {@link #CAMINHO_PADRAO} para o arquivo de save */
    public PersistenciaPlanta() {
        this(CAMINHO_PADRAO);
    }

    /**
     * Usa outro arquivo, ex.: um arquivo temporário nos testes
     *
     * @param arquivo caminho do arquivo de save
     */
    public PersistenciaPlanta(Path arquivo) {
        this.arquivo = arquivo;
    }

    /**
     * Caminho do arquivo de save
     *
     * @return caminho
     */
    public Path getArquivo() {
        return arquivo;
    }

    /**
     * Diz se já existe um save p/ carregar
     *
     * @return true se o arquivo existe e não é uma pasta
     */
    public boolean existeSave() {
        return Files.isRegularFile(arquivo);
    }

    /**
     * Quando o save foi gravado pela última vez
     *
     * @return data e hora locais, ou vazio se não houver save ou não der p/ ler a data
     */
    public Optional<LocalDateTime> dataUltimoSave() {
        try {
            return Optional.of(LocalDateTime.ofInstant(Files.getLastModifiedTime(arquivo).toInstant(), ZoneId.systemDefault()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Grava a fábrica no arquivo, substituindo o save anterior. Cria a pasta se ela não existir
     *
     * @param planta fábrica a salvar
     * @throws FalhaEscritaPersistenciaException se não der p/ gravar. O save anterior continua intacto
     */
    public void salvar(Planta planta) {
        if (Files.isDirectory(arquivo)) {
            throw new FalhaEscritaPersistenciaException("Dessa vez não é! Não deu p/ salvar: " + arquivo + " é uma pasta, não um arquivo.");
        }
        Document documento = new PlantaXmlEscritor().escrever(planta);

        Path temporario = null;
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            Files.createDirectories(pasta);
            temporario = Files.createTempFile(pasta, arquivo.getFileName().toString(), ".tmp");
            try (OutputStream saida = Files.newOutputStream(temporario)) {
                // A declaração vai à mão: o transformer do JDK cola ela na mesma linha da raiz
                saida.write(DECLARACAO_XML.getBytes(StandardCharsets.UTF_8));
                novoTransformer().transform(new DOMSource(documento), new StreamResult(saida));
            }
            substituir(temporario, arquivo);
        } catch (IOException | TransformerException e) {
            throw new FalhaEscritaPersistenciaException("Dessa vez não é! Não deu p/ salvar em " + arquivo + ": " + e.getMessage(), e);
        } finally {
            apagarSeSobrou(temporario);
        }
    }

    /**
     * Lê o arquivo e reconstrói uma fábrica nova. Nenhuma fábrica existente é alterada, mesmo se falhar
     *
     * @return fábrica nova, ainda não ativada
     * @throws ArquivoPersistenciaInexistenteException se o arquivo não existe
     * @throws ArquivoPersistenciaVazioException se o arquivo está vazio ou só tem espaços
     * @throws ArquivoPersistenciaCorrompidoException se não é um XML válido ou a estrutura não é a esperada
     * @throws DadosPersistidosInvalidosException se os dados quebram uma regra da fábrica
     * @throws FalhaLeituraPersistenciaException se o sistema não deixar ler o arquivo
     */
    public Planta carregar() {
        byte[] conteudo;
        try {
            conteudo = Files.readAllBytes(arquivo);
        } catch (NoSuchFileException e) {
            throw new ArquivoPersistenciaInexistenteException("Dessa vez não é! Nenhum save encontrado em " + arquivo + ".", e);
        } catch (IOException e) {
            throw new FalhaLeituraPersistenciaException("Dessa vez não é! Não deu p/ ler " + arquivo + ": " + e.getMessage(), e);
        }
        if (new String(conteudo, StandardCharsets.UTF_8).isBlank()) {
            throw new ArquivoPersistenciaVazioException("Dessa vez não é! O save em " + arquivo + " está vazio.");
        }

        Document documento;
        try {
            documento = PlantaXml.novoParser().parse(new ByteArrayInputStream(conteudo));
        } catch (SAXException e) {
            throw new ArquivoPersistenciaCorrompidoException("Dessa vez não é! O save está corrompido: não é um XML válido ("
                    + e.getMessage() + ").", e);
        } catch (IOException e) {
            throw new FalhaLeituraPersistenciaException("Dessa vez não é! Não deu p/ ler " + arquivo + ": " + e.getMessage(), e);
        }
        return new PlantaXmlLeitor().ler(documento);
    }

    // === Auxiliares ===

    /**
     * Transformer que grava o XML indentado em UTF-8
     *
     * @return transformer novo
     * @throws TransformerException se o JDK não tiver um transformer compatível
     */
    private static Transformer novoTransformer() throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        return transformer;
    }

    /**
     * Troca o arquivo antigo pelo novo de uma vez. Se o sistema de arquivos não suportar a troca atômica,
     * faz uma troca normal
     *
     * @param novo arquivo temporário já gravado
     * @param destino arquivo de save
     * @throws IOException se a troca falhar
     */
    private static void substituir(Path novo, Path destino) throws IOException {
        try {
            Files.move(novo, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(novo, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Apaga o temporário se a gravação parou no meio. Se nem isso der certo, só sobra um .tmp na pasta
     *
     * @param temporario arquivo temporário
     */
    private static void apagarSeSobrou(Path temporario) {
        if (temporario == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporario);
        } catch (IOException e) {
            // Não precisamos fazer nada pois o save de verdade já foi trocado ou continua o antigo
        }
    }
}
