package simpledb.tx.recovery;

import simpledb.file.Block;
import simpledb.log.BasicLogRecord;

public class UpdateRecord implements LogRecord {
	
	private int txnum;
	private String FileName;
	private int block_updated, block_saved;

	public UpdateRecord(BasicLogRecord rec) {
		// TODO Auto-generated constructor stub
		 txnum = rec.nextInt();
	     FileName = rec.nextString();
	     block_updated = rec.nextInt();
	   // blk = new Block(filename, blknum);
	   //  offset = rec.nextInt();
	    // val = rec.nextString();
	}

	@Override
	public int writeToLog() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int op() {
		// TODO Auto-generated method stub
		return UPDATE;
	}

	@Override
	public int txNumber() {
		// TODO Auto-generated method stub
		return txnum;
	}

	@Override
	public void undo(int txnum) {
		// TODO Auto-generated method stub

	}

	public void setTxnum(int txnum) {
		this.txnum = txnum;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return FileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		FileName = fileName;
	}

	/**
	 * @return the block_saved
	 */
	public int getBlock_saved() {
		return block_saved;
	}

	/**
	 * @param block_saved the block_saved to set
	 */
	public void setBlock_saved(int block_saved) {
		this.block_saved = block_saved;
	}

	/**
	 * @return the block_updated
	 */
	public int getBlock_updated() {
		return block_updated;
	}

	/**
	 * @param block_updated the block_updated to set
	 */
	public void setBlock_updated(int block_updated) {
		this.block_updated = block_updated;
	}

}
