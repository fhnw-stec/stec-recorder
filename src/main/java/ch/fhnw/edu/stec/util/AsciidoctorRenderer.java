package ch.fhnw.edu.stec.util;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;

import java.util.Map;

public final class AsciidoctorRenderer {

    /**
     * AsciiDoctor does not allow level 0 titles with the 'article' doctype, but such titles are actually allowed in
     * GitHub README.adoc files. Hacking around this mismatch when rendering the preview:
     * - Use 'book' doctype
     * - Make sure that at least one title and sub-title are present (invisible via '!')
     */
    private static final String GITHUB_HACK = "= !\n== !\n\n";

    private final Asciidoctor asciidoctor;

    public AsciidoctorRenderer() {
        asciidoctor = Asciidoctor.Factory.create();
    }

    public String renderToHtml(String asciiDocSource) {
        Map<String, Object> options = OptionsBuilder.options().docType("book").asMap();
        return asciidoctor.render(GITHUB_HACK + asciiDocSource, options);
    }

}