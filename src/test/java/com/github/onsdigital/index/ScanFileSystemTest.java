package com.github.onsdigital.index;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.onsdigital.configuration.Configuration;
import org.junit.Test;

import com.github.onsdigital.search.ScanFileSystem;

/**
 * Exercises scanning the file system
 */
public class ScanFileSystemTest {
	private String rootSearch = Configuration.CONTENT_SERVER.getContentPath();

	@Test
	public void testGetFileNames() throws IOException {
		final Path rootDir = Paths.get(rootSearch);

		List<String> fileNames = new ArrayList<String>();
		ScanFileSystem.getFileNames(fileNames, rootDir);
		assertFalse(fileNames.isEmpty());

		for (String fileName : fileNames) {
			assertTrue(
					"File extension must be one of small subset of types, e.g. html but not css",
					(fileName.endsWith(".html") || fileName.endsWith(".xls")
							|| fileName.endsWith(".xlsx")
							|| fileName.endsWith(".csv") || fileName
							.endsWith(".json")));
			assertTrue("File path must be under content folder",
					(fileName.contains(rootSearch)));

		}
	}

	@Test
	public void testGetFiles() throws IOException {
		final Path rootDir = Paths.get(rootSearch);

		List<File> files = new ArrayList<File>();
		ScanFileSystem.getFiles(files, rootDir, "bulletins");
		assertFalse(files.isEmpty());
	}

	@Test(expected = IOException.class)
	public void testUnknownRootDir() throws Exception {
		String unknownRoot = "unknown/unknown/unknown";
		final Path unknownRootDir = Paths.get(unknownRoot);
		List<String> fileNames = new ArrayList<String>();
		ScanFileSystem.getFileNames(fileNames, unknownRootDir);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullFileNames() throws Exception {
		final Path unknownRootDir = Paths.get(rootSearch);
		ScanFileSystem.getFileNames(null, unknownRootDir);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullPathDir() throws Exception {
		List<String> fileNames = new ArrayList<String>();
		ScanFileSystem.getFileNames(fileNames, null);
	}
}
