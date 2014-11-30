package simpledb.buffer;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import simpledb.file.Block;
import simpledb.file.FileMgr;
import simpledb.server.Startup;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * 
 * @author Edward Sciore
 */
class BasicBufferMgr {
	// private Buffer[] bufferpool;
	// added rkyadav
	private Map<Block, Buffer> bufferPoolMap;
	private int numAvailable;
	private int bufferSize;
	private int clockPointer;
	private int gClockMax;

	/**
	 * Creates a buffer manager having the specified number of buffer slots.
	 * This constructor depends on both the {@link FileMgr} and
	 * {@link simpledb.log.LogMgr LogMgr} objects that it gets from the class
	 * {@link simpledb.server.SimpleDB}. Those objects are created during system
	 * initialization. Thus this constructor cannot be called until
	 * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or is called
	 * first.
	 * 
	 * @param numbuffs
	 *            the number of buffer slots to allocate
	 */
	BasicBufferMgr(int numbuffs) {
		// bufferpool = new Buffer[numbuffs]; */
		// added rkyadav
		numAvailable = numbuffs;
		bufferSize = numbuffs;
		bufferPoolMap = new LinkedHashMap<Block, Buffer>();
		clockPointer = 0;
		int init = -1;
		gClockMax = Startup.Get_clock_value();
		for (int i = 0; i < numbuffs; i++) {
			Block block = new Block("dummy", init);
			bufferPoolMap.put(block, new Buffer());
			init--;
		}
		// iterator_max = 5;
		// int y = 0;
		/*
		 * for (int i=0; i<numbuffs; i++) bufferpool[i] = new Buffer();
		 */
	}

	/**
	 * Flushes the dirty buffers modified by the specified transaction.
	 * 
	 * @param txnum
	 *            the transaction's id number
	 */
	synchronized void flushAll(int txnum) {
		for (Buffer buff : bufferPoolMap.values())
			if (buff.isModifiedBy(txnum)) {
				buff.flush();
			}
		/*
		 * for (Buffer buff : bufferpool) if (buff.isModifiedBy(txnum)) {
		 * buff.flush(); }
		 */
	}

	/**
	 * Pins a buffer to the specified block. If there is already a buffer
	 * assigned to that block then that buffer is used; otherwise, an unpinned
	 * buffer from the pool is chosen. Returns a null value if there are no
	 * available buffers.
	 * 
	 * @param blk
	 *            a reference to a disk block
	 * @return the pinned buffer
	 */
	synchronized Buffer pin(Block blk) {
		System.out.println("clock Pointer : " + clockPointer);
		Buffer buff = findExistingBuffer(blk);
		if (buff == null) {
			buff = chooseUnpinnedBuffer(blk.fileName(), blk.number(), null);
			if (buff == null)
				return null;
		}
		if (!buff.isPinned())
			numAvailable--;
		buff.pin();
		System.out.println("----------Pin----------");
		System.out.println("clock Pointer : " + clockPointer);
		Set<Entry<Block, Buffer>> entrySet = bufferPoolMap.entrySet();
		for (Entry<Block, Buffer> entry : entrySet) {
			Block block = entry.getKey();
			Buffer buffer = entry.getValue();
			String output = "Filename : ";
			if(block != null) {
				output = output + block.fileName() + " | ";
				output = output + "Number : " + block.number();
			}
			if(buffer != null) {
				output = output + " | RefCount : " + buffer.getRef_counter();
				output = output + " | Pin : " + buffer.isPinned();
			}
			System.out.println(output);
		}
		System.out.println("Available : " + numAvailable);
		return buff;
	}

	/**
	 * Allocates a new block in the specified file, and pins a buffer to it.
	 * Returns null (without allocating the block) if there are no available
	 * buffers.
	 * 
	 * @param filename
	 *            the name of the file
	 * @param fmtr
	 *            a pageformatter object, used to format the new block
	 * @return the pinned buffer
	 */
	synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
		System.out.println("clock Pointer : " + clockPointer);
		Buffer buff = chooseUnpinnedBuffer(filename, -1, fmtr);
		if (buff == null)
			return null;
		numAvailable--;
		buff.pin();
		System.out.println("----------New Pin----------");
		System.out.println("clock Pointer : " + clockPointer);
		Set<Entry<Block, Buffer>> entrySet = bufferPoolMap.entrySet();
		for (Entry<Block, Buffer> entry : entrySet) {
			Block block = entry.getKey();
			Buffer buffer = entry.getValue();
			String output = "Filename : ";
			if(block != null) {
				output = output + block.fileName() + " | ";
				output = output + "Number : " + block.number();
			}
			if(buffer != null) {
				output = output + " | RefCount : " + buffer.getRef_counter();
			}
			System.out.println(output);
		}
		System.out.println("Available : " + numAvailable);
		return buff;
	}

	/**
	 * Unpins the specified buffer.
	 * 
	 * @param buff
	 *            the buffer to be unpinned
	 */
	synchronized void unpin(Buffer buff) {
		buff.unpin();
		if (!buff.isPinned()) {
			numAvailable++;
			buff.setRef_counter(5);
		}
		System.out.println("-----------UnPin----------");
		System.out.println("Available : " + numAvailable);
	}

	/**
	 * Returns the number of available (i.e. unpinned) buffers.
	 * 
	 * @return the number of available buffers
	 */
	int available() {
		return numAvailable;
	}

	private Buffer findExistingBuffer(Block blk) {
		Set<Entry<Block, Buffer>> entrySet = bufferPoolMap.entrySet();
		for (Entry<Block, Buffer> entry : entrySet) {
			Block block = entry.getKey();
			if(block.equals(blk)){
				return entry.getValue();
			}
		}
		return null;

		/*
		 * for (Buffer buff : bufferpool) { Block b = buff.block(); if (b !=
		 * null && b.equals(blk)) return buff; } return null;
		 */
	}

	private Buffer chooseUnpinnedBuffer(String fileName, int number,
			PageFormatter fmtr) {
		for (int i = 0; i < gClockMax + 1; i++) {
			Iterator<Entry<Block, Buffer>> iterator = bufferPoolMap.entrySet()
					.iterator();

			// Reach the current pointer position
			int currentPointer = 0;
			while (currentPointer != clockPointer && iterator.hasNext()) {
				iterator.next();
				currentPointer++;
			}

			while (currentPointer != bufferSize && iterator.hasNext()) {
				Entry<Block, Buffer> entry = iterator.next();
				if (!entry.getValue().isPinned()) {
					if (entry.getValue().getRef_counter() == 0) {
						clockPointer = (currentPointer + 1) % bufferSize;
						Buffer newBuffer = entry.getValue();
						Block key = entry.getKey();
						return getUpdatedBuffer(fileName, number, fmtr,
								newBuffer, key);
					} else {
						Buffer buffer = entry.getValue();
						int ref_counter = buffer.getRef_counter();
						buffer.setRef_counter(ref_counter - 1);
					}
				}
				currentPointer++;
			}

			// Reset Iterator.
			currentPointer = 0;
			iterator = bufferPoolMap.entrySet().iterator();
			while (currentPointer != clockPointer) {
				Entry<Block, Buffer> entry = iterator.next();
				if (!entry.getValue().isPinned()) {
					if (entry.getValue().getRef_counter() == 0) {
						clockPointer = (currentPointer + 1) % bufferSize;
						Buffer newBuffer = entry.getValue();
						Block key = entry.getKey();
						return getUpdatedBuffer(fileName, number, fmtr,
								newBuffer, key);
					} else {
						Buffer buffer = entry.getValue();
						int ref_counter = buffer.getRef_counter();
						buffer.setRef_counter(ref_counter - 1);
					}
				}
				currentPointer++;
			}
		}
		return null;
	}

	private Buffer getUpdatedBuffer(String fileName, int number,
			PageFormatter fmtr, Buffer newBuffer, Block key) {
		if (number == -1) {
			newBuffer.assignToNew(fileName, fmtr);
			Block newBlock = newBuffer.block();
			key.setFileName(newBlock.fileName());
			key.setNumber(newBlock.number());
		} else {
			key.setFileName(fileName);
			key.setNumber(number);
			newBuffer.assignToBlock(key);
		}
		return newBuffer;
	}

	boolean containsMapping(Block blk) {
		Buffer buffer = findExistingBuffer(blk);
		if(buffer != null) {
			return true;
		}
		return false;
	}
}
