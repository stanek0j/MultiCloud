package cz.zcu.kiv.multicloud.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.client.methods.HttpUriRequest;

import cz.zcu.kiv.multicloud.MultiCloudException;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileDownloadOp.java			<br /><br />
 *
 * Operation for downloading a file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileDownloadOp extends Operation<File> {

	/** Size of a chunk for file download. Default value is set to 4 MiB. */
	public static final long CHUNK_SIZE = 4 * 1024 * 1024;

	/** List of sources used to download the file. */
	private final List<FileCloudSource> sources;
	/** Destination to save the file to. */
	private final File destination;
	/** Thread pool of worker threads. */
	private final List<FileDownloadThread> pool;
	/** Queue with chunks for the workers. */
	private final BlockingQueue<DataChunk> queue;
	/** File writer. */
	private FileDownloadWriter writer;
	/** Progress listener. */
	private final ProgressListener listener;

	/**
	 * Ctor with necessary parameters.
	 * @param sources List of sources used to download the file.
	 * @param destination Destination to save the file to.
	 */
	public FileDownloadOp(List<FileCloudSource> sources, File destination, ProgressListener listener) {
		super(OperationType.FILE_DOWNLOAD, null, null);
		this.sources = new ArrayList<>();
		for (FileCloudSource pair: sources) {
			if (pair.getFile() != null && pair.getRequest() != null) {
				this.sources.add(pair);
			}
		}
		this.destination = destination;
		this.pool = new ArrayList<>();
		this.queue = new LinkedBlockingQueue<>();
		this.listener = listener;
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
			FileCloudSource base = sources.get(0);
			List<FileCloudSource> remove = new ArrayList<>();
			for (FileCloudSource source: sources) {
				if (!source.equals(base)) {
					if (!base.getFile().equals(source.getFile())) {
						remove.add(source);
					}
				}
			}
			sources.removeAll(remove);
			/* prepare chunks */
			long pos = 0;
			long size = base.getFile().getSize();
			listener.setTotalSize(size);
			while (size > CHUNK_SIZE) {
				queue.add(new DataChunk(pos, pos + CHUNK_SIZE));
				pos += CHUNK_SIZE;
				size -= CHUNK_SIZE;
			}
			queue.add(new DataChunk(pos, pos + size));
			/* open file for writing */
			writer = new FileDownloadWriter(destination);
			/* create threads and start them */
			for (FileCloudSource source: sources) {
				setToken(source.getToken());
				setRequest(source.getRequest());
				addPropertyMapping("id", source.getFile().getId());
				addPropertyMapping("path", source.getFile().getPath());
				HttpUriRequest request = prepareRequest(null);
				pool.add(new FileDownloadThread(queue, request, writer, listener));
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
			listener.finishTransfer();
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
