package de.sebastianschlegel.wicssard;

import com.steadystate.css.dom.*;
import com.steadystate.css.format.CSSFormat;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CssParser {

    private final static Logger LOGGER = Logger.getLogger("CssParser");

    private final Map<RgbColor, ColorModel> colors = new HashMap<>();

    private final static CSSFormat cssFormat = new CSSFormat().setRgbAsHex(true);

    public CSSStyleSheet parseCss (final Reader reader) throws IOException {
        CSSStyleSheet sheet = null;
        final InputSource source = new InputSource(reader);
        final CSSOMParser parser = new CSSOMParser(new SACParserCSS3());

        sheet = parser.parseStyleSheet(source, null, null);
        LOGGER.info("Parsed file with " + String.valueOf(sheet.getCssRules().getLength()) + " rules.");
        if (sheet.getCssRules().getLength() == 0) {
            throw new RuntimeException("No rules found. May this be no CSS file?");
        }
        return sheet;
    }

    public void parseColors (final CSSStyleSheet styleSheet) {
        this.colors.clear();
        for (int i = 0; i < styleSheet.getCssRules().getLength(); ++i) {
            final CSSRule cssRule = styleSheet.getCssRules().item(i);
            if (cssRule instanceof CSSStyleRule) {
                final CSSStyleRule styleRule = (CSSStyleRule) cssRule;
                for (int s = 0; s < styleRule.getStyle().getLength(); ++s) {
                    final String property = styleRule.getStyle().item(s);
                    final CSSValue value = styleRule.getStyle().getPropertyCSSValue(property);
                    if (value instanceof CSSValueImpl) {
                        try {
                            final RgbColor rgbColor = new RgbColor(value);
                            if (!this.colors.containsKey(rgbColor)) {
                                this.colors.put(rgbColor, new ColorModel(rgbColor));
                            }
                            this.colors.get(rgbColor).getInfo().add(styleRule.getSelectorText() + " : " + property);
                            this.colors.get(rgbColor).getReferences().add(value);
                        } catch (final DOMException ex) {
                            LOGGER.fine("Could not get rgb value: " + ex);
                        }
                    }
                }
            }
        }
        LOGGER.info("Found " + this.colors.size() + " colors");
        if (this.colors.size() == 0) {
            throw new RuntimeException("No colors found.");
        }
    }

    public Map<RgbColor, ColorModel> getColors () {
        return this.colors;
    }

    public String writeStyleSheet (final CSSStyleSheet styleSheet) {
        if (styleSheet instanceof CSSStyleSheetImpl) {
            return ((CSSStyleSheetImpl) styleSheet).getCssText(cssFormat);
        } else {
            LOGGER.severe("Could not write styleSheet " + styleSheet);
            return null;
        }
    }

    public CSSStyleSheet getDiffStyleSheet (final CSSStyleSheet original, final Collection<CSSValue> changes) {
        LOGGER.info("Writing " + changes.size() + " changes to new style sheet...");
        final CSSStyleSheetImpl result = new CSSStyleSheetImpl();
        for (int i = 0; i < original.getCssRules().getLength(); ++i) {
            final CSSRule cssRule = original.getCssRules().item(i);
            if (cssRule instanceof CSSStyleRule) {
                final CSSStyleRule styleRule = (CSSStyleRule) cssRule;
                for (int s = 0; s < styleRule.getStyle().getLength(); ++s) {
                    final String property = styleRule.getStyle().item(s);
                    final CSSValue value = styleRule.getStyle().getPropertyCSSValue(property);
                    final boolean isImportant = styleRule.getStyle().getCssText().contains("important");
                    if (changes.contains(value)) {
                        final CSSStyleRuleImpl rule = new CSSStyleRuleImpl();
                        rule.setSelectorText(styleRule.getSelectorText());
                        final CSSStyleDeclarationImpl styleDeclaration = new CSSStyleDeclarationImpl();
                        styleDeclaration.addProperty(new Property(property, value, isImportant));
                        rule.setStyle(styleDeclaration);
                        ((CSSRuleListImpl) result.getCssRules()).add(rule);
                    }
                }
            }
        }
        return result;
    }
}
