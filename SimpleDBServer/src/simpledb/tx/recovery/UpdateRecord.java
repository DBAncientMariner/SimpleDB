package simpledb.tx.recovery;

import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;
import simpledb.file.Block;
import simpledb.log.BasicLogRecord;
import simpledb.server.SimpleDB;

public class UpdateRecord implements LogRecord {

	private int txnum;
	private String filename;
	private int updatedBlockNum, backupBlockNum;

	public UpdateRecord(BasicLogRecord rec) {
		txnum = rec.nextInt();
		filename = rec.nextString();
		updatedBlockNum = rec.nextInt();
		backupBlockNum = rec.nextInt();
	}

	@Override
	public int writeToLog() {
		Object[] rec = new Object[] { UPDATE, txnum, filename,
				updatedBlockNum, backupBlockNum};
		return logMgr.append(rec);
	}

	 public UpdateRecord(int txnum, String FileName, int block_updated , int  block_saved) {
	      this.txnum = txnum;
	      this.filename = FileName;
	      this.updatedBlockNum = block_updated;
	      this.backupBlockNum = block_saved;
	   }
	public UpdateRecord(int txnum2, Block blk, int blk_no) {
		this.txnum = txnum2;
		this.filename = blk.fileName();
		this.updatedBlockNum = blk.number();
		this.backupBlockNum = blk_no;
	}

	@Override
	public int op() {
		return UPDATE;
	}

	@Override
	public int txNumber() {
		return txnum;
	}

	@Override
	public void undo(int txnum) {
		Block blk = new Block(filename,updatedBlockNum);
		BufferMgr bufferMgr = SimpleDB.bufferMgr();
		Buffer bfr = bufferMgr.pin(blk);
		bfr.restoreBlock(backupBlockNum);
		bufferMgr.unpin(bfr);
	}
	
	@Override
	public String toString() {
		return "<UPDATE " + " " + txnum + " " + filename + " " + updatedBlockNum + " " + backupBlockNum + " >";
	}
}
