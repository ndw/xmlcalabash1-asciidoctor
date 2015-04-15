package com.xmlcalabash.extensions;

import com.xmlcalabash.core.XMLCalabash;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import static org.asciidoctor.Asciidoctor.Factory.create;

import net.sf.saxon.s9api.XdmNode;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.CompatMode;
import org.asciidoctor.Options;
import org.asciidoctor.Placement;
import org.asciidoctor.SafeMode;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by ndw on 4/14/15.
 */

@XMLCalabash(
        name = "cx:asciidoctor",
        type = "{http://xmlcalabash.com/ns/extensions}asciidoctor")

public class AsciiDoctor extends DefaultStep {
        /* Attributes */
        private static final QName _allow_read_uri = new QName("", "allow-read-uri");
        private static final QName _attribute_missing = new QName("", "attribute-missing");
        private static final QName _attribute_undefined = new QName("", "attribute-undefined");
        private static final QName _backend = new QName("", "backend");
        private static final QName _title = new QName("", "title");
        private static final QName _doctype = new QName("", "doctype");
        private static final QName _imagesdir = new QName("", "imagesdir");
        private static final QName _source_language = new QName("", "source-language");
        private static final QName _source_highlighter = new QName("", "source-highlighter");
        private static final QName _max_include_depth = new QName("", "max-include-depth");
        private static final QName _sectnumlevels = new QName("", "sectnumlevels");
        private static final QName _hardbreaks = new QName("", "hardbreaks");
        private static final QName _cache_uri = new QName("", "cache-uri");
        private static final QName _hide_uri_scheme = new QName("", "hide-uri-scheme");
        private static final QName _appendix_caption = new QName("", "appendix-caption");
        private static final QName _math = new QName("", "math");
        private static final QName _skip_front_matter = new QName("", "skip-front-matter");
        private static final QName _setanchors = new QName("", "setanchors");
        private static final QName _untitled_label = new QName("", "untitled-label");
        private static final QName _ignore_undefined = new QName("", "ignore-undefined");
        private static final QName _toc_placement = new QName("", "toc-placement");
        private static final QName _toc2_placement = new QName("", "toc2-placement");
        private static final QName _showtitle = new QName("", "showtitle");
        private static final QName _toc = new QName("", "toc");
        private static final QName _localdate = new QName("", "localdate");
        private static final QName _localtime = new QName("", "localtime");
        private static final QName _docdate = new QName("", "docdate");
        private static final QName _doctime = new QName("", "doctime");
        private static final QName _stylesheet = new QName("", "stylesheet");
        private static final QName _stylesdir = new QName("", "stylesdir");
        private static final QName _linkcss = new QName("", "linkcss");
        private static final QName _copycss = new QName("", "copycss");
        private static final QName _icons = new QName("", "icons");
        private static final QName _iconfont_remote = new QName("", "iconfont-remote");
        private static final QName _iconfont_cdn = new QName("", "iconfont-cdn");
        private static final QName _iconfont_name = new QName("", "iconfont-name");
        private static final QName _data_uri = new QName("", "data-uri");
        private static final QName _iconsdir = new QName("", "iconsdir");
        private static final QName _numbered = new QName("", "numbered");
        private static final QName _linkattrs = new QName("", "linkattrs");
        private static final QName _experimental = new QName("", "experimental");
        private static final QName _nofooter = new QName("", "nofooter");
        private static final QName _compat_mode = new QName("", "compat-mode");

        /* Options */
        private static final QName _header_footer = new QName("", "header-footer");
        private static final QName _template_dirs = new QName("", "template-dirs");
        private static final QName _template_engine = new QName("", "template-engine");
        private static final QName _safe = new QName("", "safe");
        private static final QName _eruby = new QName("", "eruby");
        private static final QName _compact = new QName("", "compact");
        private static final QName _base_dir = new QName("", "base-dir");
        private static final QName _template_cache = new QName("", "template-cache");
        private static final QName _parse_header_only = new QName("", "parse-header-only");

        private ReadablePipe source = null;
        private WritablePipe result = null;

        public AsciiDoctor(XProcRuntime runtime, XAtomicStep step) {
                super(runtime, step);
        }

        public void setInput(String port, ReadablePipe pipe) {
                source = pipe;
        }

        public void setOutput(String port, WritablePipe pipe) {
                result = pipe;
        }

        public void reset() {
                source.resetReader();
                result.resetWriter();
        }

        public void run() throws SaxonApiException {
                super.run();

                XdmNode doc = source.read();
                String asciiDoc = doc.getStringValue();

                Attributes adAttr = attributes();
                Options adOpts = options();

                adOpts.setAttributes(adAttr);

                Asciidoctor asciidoctor = create();
                String doctored = asciidoctor.convert(asciiDoc, adOpts);

                try {
                        ByteArrayInputStream doctoredBytes = new ByteArrayInputStream(doctored.getBytes("UTF-8"));

                        XMLReader reader = XMLReaderFactory.createXMLReader();
                        reader.setEntityResolver(runtime.getResolver());
                        SAXSource saxSource = new SAXSource(reader, new InputSource(doctoredBytes));
                        DocumentBuilder builder = runtime.getProcessor().newDocumentBuilder();
                        builder.setLineNumbering(true);
                        builder.setDTDValidation(false);
                        builder.setBaseURI(doc.getBaseURI());

                        result.write(builder.build(saxSource));
                } catch (Exception e) {
                        throw new XProcException(e);
                }
        }

        private Attributes attributes() {
                Attributes adAttr = new Attributes();

                String s = null;

                s = getOption(_allow_read_uri, (String) null);
                if (s != null) {
                        adAttr.setAllowUriRead(bool(s));
                }

                s = getOption(_attribute_missing, (String) null);
                if (s != null) {
                        adAttr.setAttributeMissing(s);
                }

                s = getOption(_attribute_undefined, (String) null);
                if (s != null) {
                        adAttr.setAttributeUndefined(s);
                }

                s = getOption(_backend, (String) null);
                if (s != null) {
                        adAttr.setBackend(s);
                }

                s = getOption(_title, (String) null);
                if (s != null) {
                        adAttr.setTitle(s);
                }

                s = getOption(_doctype, (String) null);
                if (s != null) {
                        adAttr.setDocType(s);
                }

                s = getOption(_imagesdir, (String) null);
                if (s != null) {
                        adAttr.setImagesDir(s);
                }

                s = getOption(_source_language, (String) null);
                if (s != null) {
                        adAttr.setSourceLanguage(s);
                }

                s = getOption(_source_highlighter, (String) null);
                if (s != null) {
                        adAttr.setSourceHighlighter(s);
                }

                s = getOption(_max_include_depth, (String) null);
                if (s != null) {
                        adAttr.setMaxIncludeDepth(Integer.parseInt(s));
                }

                s = getOption(_sectnumlevels, (String) null);
                if (s != null) {
                        adAttr.setSectNumLevels(Integer.parseInt(s));
                }

                s = getOption(_hardbreaks, (String) null);
                if (s != null) {
                        adAttr.setHardbreaks(bool(s));
                }

                s = getOption(_cache_uri, (String) null);
                if (s != null) {
                        adAttr.setCacheUri(bool(s));
                }

                s = getOption(_hide_uri_scheme, (String) null);
                if (s != null) {
                        adAttr.setHideUriScheme(bool(s));
                }

                s = getOption(_appendix_caption, (String) null);
                if (s != null) {
                        adAttr.setAppendixCaption(s);
                }

                s = getOption(_math, (String) null);
                if (s != null) {
                        adAttr.setMath(s);
                }

                s = getOption(_skip_front_matter, (String) null);
                if (s != null) {
                        adAttr.setSkipFrontMatter(bool(s));
                }

                s = getOption(_setanchors, (String) null);
                if (s != null) {
                        adAttr.setAnchors(bool(s));
                }

                s = getOption(_untitled_label, (String) null);
                if (s != null) {
                        adAttr.setUntitledLabel(s);
                }

                s = getOption(_ignore_undefined, (String) null);
                if (s != null) {
                        adAttr.setIgnoreUndefinedAttributes(bool(s));
                }

                s = getOption(_toc_placement, (String) null);
                if (s != null) {
                        if ("left".equals(s)) {
                                adAttr.setTableOfContents(Placement.LEFT);
                        } else if ("right".equals(s)) {
                                adAttr.setTableOfContents(Placement.RIGHT);
                        } else if ("top".equals(s)) {
                                adAttr.setTableOfContents(Placement.TOP);
                        } else if ("bottom".equals(s)) {
                                adAttr.setTableOfContents(Placement.BOTTOM);
                        } else {
                                throw new XProcException("Invalid TOC placement value: " + s);
                        }
                }

                s = getOption(_toc2_placement, (String) null);
                if (s != null) {
                        if ("left".equals(s)) {
                                adAttr.setTableOfContents2(Placement.LEFT);
                        } else if ("right".equals(s)) {
                                adAttr.setTableOfContents2(Placement.RIGHT);
                        } else if ("top".equals(s)) {
                                adAttr.setTableOfContents2(Placement.TOP);
                        } else if ("bottom".equals(s)) {
                                adAttr.setTableOfContents2(Placement.BOTTOM);
                        } else {
                                throw new XProcException("Invalid TOC placement value: " + s);
                        }
                }

                s = getOption(_showtitle, (String) null);
                if (s != null) {
                        adAttr.setShowTitle(bool(s));
                }

                s = getOption(_toc, (String) null);
                if (s != null) {
                        adAttr.setTableOfContents(bool(s));
                }

                s = getOption(_localdate, (String) null);
                if (s != null) {
                        DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
                        adAttr.setLocalDate(parser.parseDateTime(s).toLocalDate().toDate());
                }

                s = getOption(_localtime, (String) null);
                if (s != null) {
                        DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
                        adAttr.setLocalTime(parser.parseDateTime(s).toLocalDate().toDate());
                }

                s = getOption(_docdate, (String) null);
                if (s != null) {
                        DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
                        adAttr.setDocDate(parser.parseDateTime(s).toLocalDate().toDate());
                }

                s = getOption(_doctime, (String) null);
                if (s != null) {
                        DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
                        adAttr.setDocTime(parser.parseDateTime(s).toLocalDate().toDate());
                }

                s = getOption(_stylesheet, (String) null);
                if (s != null) {
                        adAttr.setStyleSheetName(s);
                }

                s = getOption(_stylesdir, (String) null);
                if (s != null) {
                        adAttr.setStylesDir(s);
                }

                s = getOption(_linkcss, (String) null);
                if (s != null) {
                        adAttr.setLinkCss(bool(s));
                }

                s = getOption(_copycss, (String) null);
                if (s != null) {
                        adAttr.setCopyCss(bool(s));
                }

                s = getOption(_icons, (String) null);
                if (s != null) {
                        adAttr.setIcons(s);
                }

                s = getOption(_iconfont_remote, (String) null);
                if (s != null) {
                        adAttr.setIconFontRemote(bool(s));
                }

                s = getOption(_iconfont_cdn, (String) null);
                if (s != null) {
                        try {
                                adAttr.setIconFontCdn(new URI(s));
                        } catch (URISyntaxException e) {
                                throw new XProcException(e);
                        }
                }

                s = getOption(_iconfont_name, (String) null);
                if (s != null) {
                        adAttr.setIconFontName(s);
                }

                s = getOption(_data_uri, (String) null);
                if (s != null) {
                        adAttr.setDataUri(bool(s));
                }

                s = getOption(_iconsdir, (String) null);
                if (s != null) {
                        adAttr.setIconsDir(s);
                }

                s = getOption(_numbered, (String) null);
                if (s != null) {
                        adAttr.setSectionNumbers(bool(s));
                }

                s = getOption(_linkattrs, (String) null);
                if (s != null) {
                        adAttr.setLinkAttrs(bool(s));
                }

                s = getOption(_experimental, (String) null);
                if (s != null) {
                        adAttr.setExperimental(bool(s));
                }

                s = getOption(_nofooter, (String) null);
                if (s != null) {
                        adAttr.setNoFooter(bool(s));
                }

                s = getOption(_compat_mode, (String) null);
                if (s != null) {
                        if ("default".equals(s)) {
                                adAttr.setCompatMode(CompatMode.DEFAULT);
                        } else if ("legacy".equals(s)) {
                                adAttr.setCompatMode(CompatMode.LEGACY);
                        } else {
                                throw new XProcException("Invalid compat-mode value: " + s);
                        }
                }

                return adAttr;
        }

        private Options options() {
                Options adOpts = new Options();

                String s = null;
                Boolean b = null;

                s = getOption(_header_footer, (String) null);
                if (s != null) {
                        adOpts.setHeaderFooter(bool(s));
                }

                s = getOption(_template_dirs, (String) null);
                if (s != null) {
                        String[] dirs = s.split("\\s+");
                        adOpts.setTemplateDirs(dirs);
                }

                s = getOption(_template_engine, (String) null);
                if (s != null) {
                        adOpts.setTemplateEngine(s);
                }

                s = getOption(_safe, (String) null);
                if (s != null) {
                        if ("safe".equals(s)) {
                                adOpts.setSafe(SafeMode.SAFE);
                        } else if ("unsafe".equals(s)) {
                                adOpts.setSafe(SafeMode.UNSAFE);
                        } else if ("server".equals(s)) {
                                adOpts.setSafe(SafeMode.SERVER);
                        } else if ("secure".equals(s)) {
                                adOpts.setSafe(SafeMode.SECURE);
                        } else {
                                throw new XProcException("Invalid safe value: " + s);
                        }
                }

                s = getOption(_eruby, (String) null);
                if (s != null) {
                        adOpts.setEruby(s);
                }

                s = getOption(_compact, (String) null);
                if (s != null) {
                        adOpts.setCompact(bool(s));
                }

                s = getOption(_base_dir, (String) null);
                if (s != null) {
                        adOpts.setBaseDir(s);
                }

                s = getOption(_template_cache, (String) null);
                if (s != null) {
                        adOpts.setTemplateCache(bool(s));
                }

                s = getOption(_parse_header_only, (String) null);
                if (s != null) {
                        adOpts.setParseHeaderOnly(bool(s));
                }

                return adOpts;
        }
        
        private boolean bool(String s) {
                if ("true".equals(s)) {
                        return true;
                } else if ("false".equals(s)) {
                        return false;
                } else {
                        throw new XProcException("Invalid boolean value: " + s);
                }
        }
}
