package simpledb.buffer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import simpledb.file.Block;
import simpledb.file.FileMgr;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class BasicBufferMgr {
   //private Buffer[] bufferpool;
   //added rkyadav
   private Map<Block,Buffer> bufferPoolMap;
   private int numAvailable;
   private int iterator_main;
   /**
    * Creates a buffer manager having the specified number 
    * of buffer slots.
    * This constructor depends on both the {@link FileMgr} and
    * {@link simpledb.log.LogMgr LogMgr} objects 
    * that it gets from the class
    * {@link simpledb.server.SimpleDB}.
    * Those objects are created during system initialization.
    * Thus this constructor cannot be called until 
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * is called first.
    * @param numbuffs the number of buffer slots to allocate
    */
   BasicBufferMgr(int numbuffs) {
     /* bufferpool = new Buffer[numbuffs];*/
      //added rkyadav
	  numAvailable = numbuffs;
      bufferPoolMap=new HashMap<Block,Buffer>();
      iterator_main = 0;
      for (int i=0; i<numbuffs; i++)
    	  bufferPoolMap.put(new Buffer().block(),new Buffer());
      
      /*for (int i=0; i<numbuffs; i++)
         bufferpool[i] = new Buffer();*/
   }
   
   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   synchronized void flushAll(int txnum) {
	   for (Buffer buff : bufferPoolMap.values())
	         if (buff.isModifiedBy(txnum))
	         {
	        	 buff.flush();
	         }
      /*for (Buffer buff : bufferpool)
         if (buff.isModifiedBy(txnum))
         {
        	 buff.flush();
         }*/
   }
   
   /**
    * Pins a buffer to the specified block. 
    * If there is already a buffer assigned to that block
    * then that buffer is used;  
    * otherwise, an unpinned buffer from the pool is chosen.
    * Returns a null value if there are no available buffers.
    * @param blk a reference to a disk block
    * @return the pinned buffer
    */
   synchronized Buffer pin(Block blk) {
      Buffer buff = findExistingBuffer(blk);
      if (buff == null) {
         buff = chooseUnpinnedBuffer();
         if (buff == null)
            return null;
         buff.assignToBlock(blk);
      }
      if (!buff.isPinned())
         numAvailable--;
      buff.pin();
      return buff;
   }
   
   /**
    * Allocates a new block in the specified file, and
    * pins a buffer to it. 
    * Returns null (without allocating the block) if 
    * there are no available buffers.
    * @param filename the name of the file
    * @param fmtr a pageformatter object, used to format the new block
    * @return the pinned buffer
    */
   synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
      Buffer buff = chooseUnpinnedBuffer();
      if (buff == null)
         return null;
      buff.assignToNew(filename, fmtr);
      numAvailable--;
      buff.pin();
      return buff;
   }
   
   /**
    * Unpins the specified buffer.
    * @param buff the buffer to be unpinned
    */
   synchronized void unpin(Buffer buff) {
      buff.unpin();
      if (!buff.isPinned())
         numAvailable++;
      buff.setRef_counter(5);
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
      return numAvailable;
   }
   
   private Buffer findExistingBuffer(Block blk) {
	   //added rkyadav
	   if(bufferPoolMap.containsKey(blk))
	   {
		   return bufferPoolMap.get(blk);
	   }
	   else return null;
	   /*
      for (Buffer buff : bufferpool) {
         Block b = buff.block();
         if (b != null && b.equals(blk))
            return buff;
      }
      return null;
      */
   }
   
   private Buffer chooseUnpinnedBuffer() {
	   //modified rkyadav
	   int count = 0;
	   int start = 0;
	   int limit = 0;
	   int iterator_gclock = iterator_main;
	   while(limit<(numAvailable*5)){
		   
		   Iterator iterator = bufferPoolMap.entrySet().iterator();
		   Buffer buff;
		   iterator_gclock--;
		   while(iterator.hasNext() && (iterator_gclock < 0) && limit<(numAvailable*5))
		   {
			   limit++;
			  Map.Entry pairs = (Map.Entry)iterator.next();
			  buff = (Buffer) pairs.getValue();
			  
			  if (!buff.isPinned() && buff.getRef_counter() == 0)
			  {
				  iterator_main = (iterator_main + limit) % numAvailable;
				   return buff;
			  }
			  else if (!buff.isPinned() && buff.getRef_counter() != 0)
			  {
				  int curr = buff.getRef_counter();
				  buff.setRef_counter(curr-1);
			  }
		   
		  }
	   }
      /*for (Buffer buff : bufferpool)
         if (!buff.isPinned())
         return buff;*/
      return null;
   }
   //added rkyadav
   boolean containsMapping(Block blk) {  
	   return bufferPoolMap.containsKey(blk);  
   } 

}
