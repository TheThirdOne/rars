package rars.program.elf;

/*
 * 	Code from https://github.com/fornwall/jelf
 * 
 * 	Copyright (c) 2016-2017 Fredrik Fornwall.
 *
 *	Permission is hereby granted, free of charge, to any person obtaining 
 *	a copy of this software and associated documentation files (the "Software"), 
 *	to deal in the Software without restriction, including without limitation 
 *	the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 *	and/or sell copies of the Software, and to permit persons to whom the 
 *	Software is furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in 
 *	all copies or substantial portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 *	SOFTWARE.
 */

import java.io.IOException;

/**
 * A memoized object. Override {@link #computeValue} in subclasses; call {@link #getValue} in using code.
 */
abstract class MemoizedObject<T> {
	private boolean computed;
	private T value;

	/**
	 * Should compute the value of this memoized object. This will only be called once, upon the first call to
	 * {@link #getValue}.
	 */
	protected abstract T computeValue() throws ElfException, IOException;

	/** Public accessor for the memoized value. */
	public final T getValue() throws ElfException, IOException {
		if (!computed) {
			value = computeValue();
			computed = true;
		}
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> MemoizedObject<T>[] uncheckedArray(int size) {
		return new MemoizedObject[size];
	}
}