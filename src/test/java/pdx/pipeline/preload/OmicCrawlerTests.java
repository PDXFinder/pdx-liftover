package pdx.pipeline.preload;

import org.junit.*;
import pdx.pipeline.OmicCrawler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class OmicCrawlerTests {

    private String finderRootDir;


    private Path rootDir;
    private Path dataDir;
    private Path updogDir;
    private Path provider1;
    private Path provider2;
    private Path incorrectMut;
    private Path metadata;
    private Path incorrectCyto;
    private Path mut2;
    private Path metadata2;
    private Path cyto2;
    private Path incorrectCna;
    private Path cna1;
    private Path mutFolder;
    private Path cnaFolder;
    private Path cytoFolder;
    private Path mutFolder2;
    private Path cnaFolder2;
    private Path cytoFolder2;

    @Before
    public void buildTestStructure() throws IOException {

        rootDir = Files.createTempDirectory(("TEST"));
        dataDir = Files.createDirectory(Paths.get(rootDir.toString() + "/data"));
        updogDir = Files.createDirectory(Paths.get(dataDir.toString() + "/UPDOG"));

        provider1 = Files.createDirectory(Paths.get(updogDir.toString() + "/provider1"));
        provider2 = Files.createDirectory(Paths.get(updogDir.toString() + "/provider2"));

        mutFolder = Files.createDirectory(Paths.get(provider1.toString() + "/mut"));
        cnaFolder = Files.createDirectory(Paths.get(provider1.toString() + "/cna"));
        cytoFolder = Files.createDirectory(Paths.get(provider1.toString() + "/cyto"));

        mutFolder2 = Files.createDirectory(Paths.get(provider2.toString() + "/mut"));
        cnaFolder2 = Files.createDirectory(Paths.get(provider2.toString() + "/cna"));
        cytoFolder2 = Files.createDirectory(Paths.get(provider2.toString() + "/cyto"));

        mut2 = Files.createFile(Paths.get(mutFolder2.toString() + "/test_mut.tsv"));
        metadata2 = Files.createFile(Paths.get(provider2.toString() + "/metadata.xlsx"));
        cyto2 = Files.createFile(Paths.get(cytoFolder2.toString() + "/_cytogenetics.tsv"));
        cna1 = Files.createFile(Paths.get(cnaFolder2.toString() + "/test_cna.tsv"));
    }

    @After
    public void deleteFiles() {

        mut2.toFile().delete();
        metadata2.toFile().delete();
        cyto2.toFile().delete();
        cna1.toFile().delete();
        mutFolder.toFile().delete();
        cnaFolder.toFile().delete();
        cytoFolder.toFile().delete();
        mutFolder2.toFile().delete();
        cnaFolder2.toFile().delete();
        cytoFolder2.toFile().delete();
        provider1.toFile().delete();
        provider2.toFile().delete();
        dataDir.toFile().delete();
        updogDir.toFile().delete();
    }

    @Test(expected = IOException.class)
    public void Given_nonExistentRootFolder_When_crawlerIsCalled_throwIOError() throws IOException {

        //when
        initCrawlersAndPassRootFile(new File(""));
    }

    @Test
    public void Given_productionFileSchema_When_crawlerIsCalled_returnListOfOnlyMutAndCNA() throws IOException {

        //given
        //init()
        incorrectMut = Files.createFile(Paths.get(mutFolder.toString() + "/data.xlsx"));
        metadata = Files.createFile(Paths.get(provider1.toString() + "/metadata.xlsx"));
        incorrectCyto = Files.createFile(Paths.get(cytoFolder.toString() + "/data.xlsx"));
        incorrectCna = Files.createFile(Paths.get(cnaFolder.toString() + "/data.xlsx"));


        //when
        List<File> actualFiles = initCrawlersAndPassRootFile(rootDir.toFile());

        //then
        Assert.assertEquals(2, actualFiles.size());
        Assert.assertTrue(actualFiles.contains(mut2.toFile()));
        Assert.assertTrue(actualFiles.contains(cna1.toFile()));
    }

    @Test
    public void Given_invalidfileName_When_crawlerIsCalled_returnOnlyValidNameMutAndCNA() throws IOException {

        //given
        incorrectMut = Files.createFile(Paths.get(mutFolder.toString() + "/TESTdata.xlsx"));
        metadata = Files.createFile(Paths.get(provider1.toString() + "/metadata.xlsx"));
        incorrectCyto = Files.createFile(Paths.get(cytoFolder.toString() + "/datahello.xlsx"));
        incorrectCna = Files.createFile(Paths.get(cnaFolder.toString() + "/'~data.xlsx'"));

        //when
        List<File> actualFiles = initCrawlersAndPassRootFile(rootDir.toFile());

        //then
        Assert.assertEquals(2, actualFiles.size());
        Assert.assertFalse(actualFiles.contains(incorrectMut.toFile()));
        Assert.assertTrue(actualFiles.contains(mut2.toFile()));
        Assert.assertFalse(actualFiles.contains(incorrectCna.toFile()));
        Assert.assertTrue(actualFiles.contains(cna1.toFile()));
    }

    private List<File> initCrawlersAndPassRootFile(File rootDir) throws IOException {

        OmicCrawler crawler = new OmicCrawler();
        return crawler.run(rootDir);
    }
}
