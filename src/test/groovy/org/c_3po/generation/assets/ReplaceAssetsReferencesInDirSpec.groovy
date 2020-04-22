package org.c_3po.generation.assets

import org.c_3po.io.Directories
import org.jsoup.Jsoup
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

import static org.c_3po.generation.SiteGenerationHelpers.ensureDestinationDirIsClean
import static org.c_3po.generation.SiteGenerationHelpers.generateSite

class ReplaceAssetsReferencesInDirSpec extends Specification {
    @Shared srcDir = Paths.get("src/test/resources/test-project-src")
    @Shared destDir = Paths.get("src/test/resources/test-project-build")

    def assetSubstitutes = [
            '/css/main.css': '/css/main.6180d1743d1be0d975ed1afbdc3b4c0bfb134124.css',
            '/css/vendor/normalize.css': '/css/vendor/normalize.05802ba9503c8a062ee85857fc774d41e96d3a80.css'
    ]
    def generatorSettings = new Properties()
    def destDirClone = Files.createTempDirectory("c-3po_dest-dir-for-specs_")

    def setupSpec() {
        ensureDestinationDirIsClean(destDir)
        generateSite(srcDir, destDir, false)
    }

    def setup() {
        // Clone dir so that each feature works on a clean slate
        Directories.copyDir(destDir, destDirClone)

        // TODO: If ever reading out settings is ever more than that, e.g. coercing default values,
        //  be sure to use the corresponding function that is called in application code as well.
        generatorSettings.load(Files.newInputStream(srcDir.resolve(".c3posettings")))
    }

    def "replaces stylesheet references" () {
        when:
        AssetReferences.replaceAssetsReferences(destDirClone, assetSubstitutes, generatorSettings)

        then:
        assertRefsReplacedIn("blog.html")
        assertRefsReplacedIn("about.html")
    }

    def "replaces asset references in HTML files located in sub directories" () {
        when:
        AssetReferences.replaceAssetsReferences(destDirClone, assetSubstitutes, generatorSettings)

        then:
        assertRefsReplacedIn("blog/first-blog-post.html")
    }

    def cleanup() {
        destDirClone.toFile().deleteDir()
    }

    void assertRefsReplacedIn(htmlFilePath) {
        def doc = Jsoup.parse(destDirClone.resolve(htmlFilePath).toFile(), "UTF-8")
        def elements = doc.select("link[rel='stylesheet']")

        // Note: Assertions are strongly coupled to the order the stylesheets are referenced
        // in the test project. Thus, obviously Jsoup returns elements in document order.
        assert elements.get(0).attr("href") == '/css/main.6180d1743d1be0d975ed1afbdc3b4c0bfb134124.css'
        assert elements.get(1).attr("href") == '/css/vendor/normalize.05802ba9503c8a062ee85857fc774d41e96d3a80.css'
    }
}
