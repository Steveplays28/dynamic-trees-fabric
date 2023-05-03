package io.github.steveplays28.dynamictreesfabric.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import io.github.steveplays28.dynamictreesfabric.api.resource.TreeResourcePack;
import io.github.steveplays28.dynamictreesfabric.util.CommonCollectors;
import net.minecraftforge.resource.PathPackResources;

import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.packs.ResourcePackFileNotFoundException;
import net.minecraft.util.Identifier;

/**
 * Credits: A lot of the file reading code was based off {@link PathPackResources}.
 *
 * @author Harley O'Connor
 */
public class FlatTreeResourcePack extends AbstractFileResourcePack implements TreeResourcePack {

	protected final Path path;

	public FlatTreeResourcePack(final Path path) {
		super(new File("dummy"));
		this.path = path;
	}

	@Override
	public InputStream open(@Nullable ResourceType type, Identifier location) throws IOException {
		final Path path = this.getPath(location.getNamespace(), location.getPath());
		if (!Files.exists(path)) {
			throw new FileNotFoundException("Could not find tree resource for path '" + path + "'.");
		}
		return Files.newInputStream(path, StandardOpenOption.READ);
	}

	@Override
	protected InputStream openFile(String resourcePath) throws IOException {
		// We never use this method, so just throw an exception.
		throw new ResourcePackFileNotFoundException(this.file, resourcePath);
	}

	@Override
	public boolean hasResource(Identifier location) {
		return Files.exists(this.getPath(location.getNamespace(), location.getPath()));
	}

	@Override
	protected boolean hasResource(String resourcePath) {
		// We never use this method, so just return false.
		return false;
	}

	@Override
	public Collection<Identifier> findResources(@Nullable ResourceType type, String namespace, String pathIn, Predicate<Identifier> filter) {
		try {
			Path root = this.getPath(namespace);
			Path inputPath = root.getFileSystem().getPath(pathIn);

			return Files.walk(root)
					.map(path -> root.relativize(path.toAbsolutePath()))
					.filter(path -> !path.toString().endsWith(".mcmeta") && path.startsWith(inputPath))
					// It is VERY IMPORTANT that we do not rely on Path.toString as this is inconsistent between operating systems
					// Join the path names ourselves to force forward slashes #8813
					.filter(path -> Identifier.isPathValid(Joiner.on('/').join(path))) // Only process valid paths Fixes the case where people put invalid resources in their jar.
					.map(path -> new Identifier(namespace, Joiner.on('/').join(path)))
					.filter(filter)
					.collect(CommonCollectors.toAlternateLinkedSet());
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public Set<String> getNamespaces(@Nullable final ResourceType type) {
		try {
			Path root = this.getPath();

			return Files.walk(root, 1)
					.map(path -> root.relativize(path.toAbsolutePath()))
					.filter(path -> path.getNameCount() > 0) // skip the root entry
					.map(p -> p.toString().replaceAll("/$", "")) // remove the trailing slash, if present
					.filter(s -> !s.isEmpty()) // filter empty strings, otherwise empty strings default to minecraft in ResourceLocations
					.collect(CommonCollectors.toLinkedSet());
		} catch (IOException e) {
			return Collections.emptySet();
		}
	}

	protected Path getPath(final String... paths) {
		return this.path.getFileSystem().getPath(this.path.toString(), paths);
	}

	@Override
	public void close() {
	}

}
