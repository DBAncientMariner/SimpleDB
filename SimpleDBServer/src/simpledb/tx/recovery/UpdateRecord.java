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
		block_saved = rec.nextInt();
		// blk = new Block(filename, blknum);
		// offset = rec.nextInt();
		// val = rec.nextString();
	}

	@Override
	public int writeToLog() {
		Object[] rec = new Object[] { UPDATE, txnum, FileName,
				block_updated, block_saved};
		return logMgr.append(rec);
	}

	 public UpdateRecord(int txnum, String FileName, int block_updated , int  block_saved) {
	      this.txnum = txnum;
	      this.FileName = FileName;
	      this.block_updated = block_updated;
	      this.block_saved = block_saved;
	   }
	public UpdateRecord(int txnum2, Block blk, int blk_no) {
		// TODO Auto-generated constructor stub
		this.txnum = txnum2;
		this.FileName = blk.fileName();
		this.block_updated = blk.number();
		this.block_saved = blk_no;
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
		Block blk = new Block(FileName,block_updated);
		simpledb.buffer.Buffer bfr = new simpledb.buffer.Buffer();
		bfr.restoreBlock(blk,block_saved);

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
	 * @param fileName
	 *            the fileName to set
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
	 * @param block_saved
	 *            the block_saved to set
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
	 * @param block_updated
	 *            the block_updated to set
	 */
	public void setBlock_updated(int block_updated) {
		this.block_updated = block_updated;
	}

}
