package com.github.onsdigital.babbage.feedback;

import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.github.onsdigital.babbage.util.TestsUtil.setPrivateField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by dave on 9/1/16.
 */
public class FileSystemFeedbackRepositoryTest {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

    @Mock
    private FeedbackForm mockForm;

    @Mock
    private SlackFeedbackNotifier mockNotifier;

    @Mock
    private FeedbackWriter feedbackWriterMock;

    private Supplier<Path> testNameGenerator;
    private List<Path> fileNamesList;
    private AtomicInteger fileNames;

    private FeedbackRepository repo;
    private Path feedbackBaseDir;
    private Path feedbackDir;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        fileNames = new AtomicInteger(10000);

        feedbackBaseDir = Files.createTempDirectory("feedback");
        feedbackDir = feedbackBaseDir.resolve(SDF.format(new Date()));

        System.setProperty("feedback_folder", feedbackBaseDir.toString());

        fileNamesList = new ArrayList<>();

        testNameGenerator = () -> {
            fileNamesList.add(feedbackDir.resolve(String.valueOf(fileNames.incrementAndGet()) + ".json"));
            return fileNamesList.get(fileNamesList.size() - 1);
        };

        repo = new FileSystemFeedbackRepository(feedbackBaseDir, mockNotifier);
    }

    @Test
    public void shouldNotSaveFeedbackForNullForm() throws Exception {
        setPrivateField(repo, "nameGenerator", testNameGenerator);
        setPrivateField(repo, "feedbackWriter", feedbackWriterMock);

        repo.save(null);

        verify(feedbackWriterMock, never())
                .write(any(FeedbackForm.class), any(Path.class));
    }

    @Test
    public void shouldNotSaveFeedbackForEmptyForm() throws Exception {
        setPrivateField(repo, "nameGenerator", testNameGenerator);
        setPrivateField(repo, "feedbackWriter", feedbackWriterMock);

        repo.save(new FeedbackForm());

        verify(feedbackWriterMock, never())
                .write(any(FeedbackForm.class), any(Path.class));
    }

    @Test
    public void shouldSaveFeedback() throws Exception {
        FeedbackForm form = new FeedbackForm()
                .feedback("Generic comment")
                .emailAddress("jillValentine@RacoonCity.com")
                .found("yes")
                .understood("no")
                .uri("/one/two/three");

        setPrivateField(repo, "nameGenerator", testNameGenerator);

        repo.save(form);

        for (File file : Arrays.asList(feedbackDir.toFile().listFiles())) {
            Path path = file.toPath();
            assertThat("EXPECTED sefHsglh", fileNamesList.contains(path));
        }
    }

    @After
    public void cleanUp() throws IOException {
        if (Files.exists(feedbackBaseDir)) {
            FileUtils.deleteDirectory(feedbackBaseDir.toFile());
        }
    }

}


