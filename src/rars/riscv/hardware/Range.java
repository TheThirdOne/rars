package rars.riscv.hardware;

public class Range {
    public final int high, low;
    public Range(int low, int high){
        if(Integer.compareUnsigned(low,high) > 0) throw new IllegalArgumentException();
        this.high = high;
        this.low = low;
    }
    public boolean contains(int ptr){
        return Integer.compareUnsigned(low,ptr) <= 0 && 0 <= Integer.compareUnsigned(high,ptr);
    }
    public Range combine(Range other){
        int low = (Integer.compareUnsigned(this.low,other.low) < 0) ?this.low:other.low;
        int high = (Integer.compareUnsigned(this.high,other.high) > 0) ?this.high:other.high;
        return new Range(low,high);
    }
}