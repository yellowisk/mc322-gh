package br.unicamp.ic.mc322.persistence;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * O que o {@link PlantaXmlEscritor} e o {@link PlantaXmlLeitor} compartilham: versão do formato e o
 * parser de XML
 *
 * @author Heitor Almeida
 */
final class PlantaXml {
    /** Versão do formato. O leitor recusa arquivos de outra versão */
    static final String VERSAO = "1";

    /** Só tem membros static, não é p/ instanciar */
    private PlantaXml() {
    }

    /**
     * Parser de XML padrão do JDK
     *
     * @return parser novo
     */
    static DocumentBuilder novoParser() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("O JDK não tem um parser de XML compatível", e);
        }
    }
}
