package cz.zcu.kiv.multicloud.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.client.methods.HttpUriRequest;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileDownloadOp.java
 *
 * Operation for downloading a file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileDownloadOp extends Operation<File> {

	/** Size of a chunk for file download. */
	public static final long CHUNK_SIZE = 4 * 128 * 1024;

	private final List<FileCloudPair> sources;
	private final File destination;
	private final List<FileDownloadThread> pool;
	private final BlockingQueue<DataChunk> queue;
	private FileDownloadWriter writer;

	public FileDownloadOp(OAuth2Token token, List<FileCloudPair> sources, File destination) {
		super(OperationType.FILE_DOWNLOAD, token, null);
		this.sources = new ArrayList<>();
		for (FileCloudPair pair: sources) {
			if (pair.getFile() != null && pair.getRequest() != null) {
				this.sources.add(pair);
			}
		}
		this.destination = destination;
		this.pool = new ArrayList<>();
		this.queue = new LinkedBlockingQueue<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationBegin() throws MultiCloudException {
		/* no preparation necessary */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationExecute() throws MultiCloudException {
		if (sources.size() > 0) {
			/* remove inconsistent files */
			FileCloudPair base = sources.get(0);
			List<FileCloudPair> remove = new ArrayList<>();
			for (FileCloudPair pair: sources) {
				if (!pair.equals(base)) {
					if (!base.getFile().equals(pair.getFile())) {
						remove.add(pair);
					}
				}
			}
			sources.removeAll(remove);
			/* prepare chunks */
			long pos = 0;
			long size = base.getFile().getSize();
			while (size > CHUNK_SIZE) {
				queue.add(new DataChunk(pos, pos + CHUNK_SIZE));
				pos += CHUNK_SIZE;
				size -= CHUNK_SIZE;
			}
			queue.add(new DataChunk(pos, pos + size));
			/* open file for writing */
			writer = new FileDownloadWriter(destination);
			/* create threads and start them */
			for (FileCloudPair pair: sources) {
				setRequest(pair.getRequest());
				addPropertyMapping("id", pair.getFile().getId());
				addPropertyMapping("path", pair.getFile().getPath());
				HttpUriRequest request = prepareRequest(null);
				pool.add(new FileDownloadThread(queue, request, writer));
			}
			System.out.println("threads: " + pool.size());
			for (FileDownloadThread thread: pool) {
				thread.start();
			}
			for (FileDownloadThread thread: pool) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					/* join interrupted */
				}
			}
			/* close the file after writing all the data and set the result */
			writer.close();
			if (queue.isEmpty()) {
				setResult(destination);
			} else {
				throw new MultiCloudException("Failed to download the file.");
			}
		} else {
			throw new MultiCloudException("No sources specified.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationFinish() throws MultiCloudException {
		/* no finalization necessary */
	}

}
