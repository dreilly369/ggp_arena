package org.ggp.base.player.gamer.statemachine.sample;

import java.util.ArrayList;
import java.util.Iterator;

public class Queue<Key> implements Iterable<Key>{

	private ArrayList<Key> queued;

	public void enqueue(Key item){
		this.queued.add(item);
	}

	public void dequeue(){
		Object next = this.queued.get(0);
		this.queued.remove(0);
	}

	public boolean hasNext(){
		return !this.queued.isEmpty();
	}

	@Override
	public Iterator<Key> iterator() {
		return this.queued.iterator();
	}
}
