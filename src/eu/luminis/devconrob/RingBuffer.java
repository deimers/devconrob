package eu.luminis.devconrob;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * A buffer that when filled starts adding new at the beginning of the buffer
 * and overwriting the old.
 * 
 * @param <E> the type of the elements stored
 */
public class RingBuffer<E> {

	private final Queue<E> data = new ArrayDeque<>();
	private final int size;

	public RingBuffer(int size) {
		this.size = size;
	}

	public void add(E element) {
        data.add(element);
		if (data.size() > size) {
			data.poll();
		}
	}

	public boolean filled() {
	    return data.size() == size;
	}

	public List<E> elements() {
		return new ArrayList<E>(data);
	}

}
