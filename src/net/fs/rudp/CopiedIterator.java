package net.fs.rudp;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author hefan.hf
 * @version $Id: CopiedIterator, v 0.1 16/5/21 上午11:16 hefan.hf Exp $
 */
public class CopiedIterator  implements Iterator {
	private Iterator iterator = null;

	public CopiedIterator(Iterator itr) {
		LinkedList list = new LinkedList();
		while (itr.hasNext()) {
			list.add(itr.next());
		}
		this.iterator = list.iterator();
	}

	public boolean hasNext() {
		return this.iterator.hasNext();
	}

	public void remove() {
		throw new UnsupportedOperationException("This is a read-only iterator.");
	}

	public Object next() {
		return this.iterator.next();
	}
}