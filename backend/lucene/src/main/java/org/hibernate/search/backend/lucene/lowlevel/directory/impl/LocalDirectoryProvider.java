/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.lowlevel.directory.impl;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hibernate.search.backend.lucene.cfg.LuceneBackendSettings;
import org.hibernate.search.backend.lucene.logging.impl.Log;
import org.hibernate.search.backend.lucene.lowlevel.directory.FileSystemAccessStrategyName;
import org.hibernate.search.backend.lucene.lowlevel.directory.spi.DirectoryCreationContext;
import org.hibernate.search.backend.lucene.lowlevel.directory.spi.DirectoryProvider;
import org.hibernate.search.backend.lucene.lowlevel.directory.spi.DirectoryHolder;
import org.hibernate.search.backend.lucene.lowlevel.directory.spi.DirectoryProviderInitializationContext;
import org.hibernate.search.engine.cfg.spi.ConfigurationProperty;
import org.hibernate.search.engine.cfg.spi.ConfigurationPropertySource;
import org.hibernate.search.util.common.impl.SuppressingCloser;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;
import org.hibernate.search.util.common.reporting.EventContext;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSLockFactory;

public class LocalDirectoryProvider implements DirectoryProvider {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private static final ConfigurationProperty<Path> ROOT =
			ConfigurationProperty.forKey( LuceneBackendSettings.DirectoryRadicals.ROOT )
					.as( Path.class, Paths::get )
					.withDefault( () -> Paths.get( LuceneBackendSettings.Defaults.DIRECTORY_ROOT ) )
					.build();

	private static final ConfigurationProperty<FileSystemAccessStrategyName> FILESYSTEM_ACCESS_STRATEGY =
			ConfigurationProperty.forKey( LuceneBackendSettings.DirectoryRadicals.FILESYSTEM_ACCESS_STRATEGY )
					.as( FileSystemAccessStrategyName.class, FileSystemAccessStrategyName::of )
					.withDefault( LuceneBackendSettings.Defaults.DIRECTORY_FILESYSTEM_ACCESS_STRATEGY )
					.build();

	private Path root;
	private FileSystemAccessStrategy accessStrategy;

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + "root=" + root + "]";
	}

	@Override
	public void initialize(DirectoryProviderInitializationContext context) {
		ConfigurationPropertySource propertySource = context.getConfigurationPropertySource();
		this.root = ROOT.get( propertySource ).toAbsolutePath();
		FileSystemAccessStrategyName accessStrategyName = FILESYSTEM_ACCESS_STRATEGY.get( propertySource );
		this.accessStrategy = FileSystemAccessStrategy.get( accessStrategyName );
		initializeRootDirectory( root );
	}

	@Override
	public DirectoryHolder createDirectory(DirectoryCreationContext context) throws IOException {
		Path directoryPath = root.resolve( context.getIndexName() );
		makeSanityCheckedFilesystemDirectory( directoryPath, context.getEventContext() );
		// TODO HSEARCH-3440 re-allow configuring the lock factory
		//  see org.hibernate.search.store.impl.DefaultLockFactoryCreator.createLockFactory
		Directory directory = accessStrategy.createDirectory( directoryPath, FSLockFactory.getDefault() );
		try {
			context.initializeIndexIfNeeded( directory );
			return DirectoryHolder.of( directory );
		}
		catch (IOException | RuntimeException e) {
			new SuppressingCloser( e ).push( directory );
			throw e;
		}
	}

	private void initializeRootDirectory(Path rootDirectory) {
		if ( Files.exists( rootDirectory ) ) {
			if ( !Files.isDirectory( rootDirectory ) || !Files.isWritable( rootDirectory ) ) {
				throw log.localDirectoryBackendRootDirectoryNotWritableDirectory( rootDirectory );
			}
		}
		else {
			try {
				Files.createDirectories( rootDirectory );
			}
			catch (Exception e) {
				throw log.unableToCreateRootDirectoryForLocalDirectoryBackend( rootDirectory, e );
			}
		}
	}

	private void makeSanityCheckedFilesystemDirectory(Path indexDirectory, EventContext eventContext) {
		if ( Files.exists( indexDirectory ) ) {
			if ( !Files.isDirectory( indexDirectory ) || !Files.isWritable( indexDirectory ) ) {
				throw log.localDirectoryIndexRootDirectoryNotWritableDirectory( indexDirectory, eventContext );
			}
		}
		else {
			try {
				Files.createDirectories( indexDirectory );
			}
			catch (Exception e) {
				throw log.unableToCreateIndexRootDirectoryForLocalDirectoryBackend( indexDirectory, eventContext, e );
			}
		}
	}
}