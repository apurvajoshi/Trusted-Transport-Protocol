/*
 * DO NOT MODIFY ANY CLASS MEMBERS !!!
 */

package datatypes;

import java.io.Serializable;


// Format of datagram packet
public class TTPSegment implements Serializable {
	
	// Source port
	short srcport;
	
	// Destination port
	short dstport;
	
	int seqNumber;
	
	int ackNumber;
	
	//header length 
	byte headerLen = 16;
	
	//Flags
	byte flags;
	
	short rcvWindow;
	
	// Actual data
	Object data;

	
	public TTPSegment() {
		super();
	}
	
	public TTPSegment(short srcport, short dstport, int seqNumber, int ackNumber,byte headerLen, byte flags, short rcvWindow, Object data) {
		super();
		this.srcport = srcport;
		this.dstport = dstport;
		this.seqNumber = seqNumber;
		this.ackNumber = ackNumber;
		this.headerLen = headerLen;
		this.flags = flags;
		this.rcvWindow = rcvWindow;
		this.data = data;
	}

	public short getSrcport() {
		return srcport;
	}

	public void setSrcport(short srcport) {
		this.srcport = srcport;
	}
	
	public short getDstport() {
		return dstport;
	}

	public void setDstport(short dstport) {
		this.dstport = dstport;
	}

	public int getSeqNumber() {
		return seqNumber;
	}
	
	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}

	public int getAckNumber() {
		return ackNumber;
	}
	
	public void setAckNumber(int ackNumber) {
		this.ackNumber = ackNumber;
	}
	
	public byte getHeaderLen() {
		return headerLen;
	}
	
	public void setHeaderLen(byte headerLen) {
		this.headerLen = headerLen;
	}

	public byte getFlags() {
		return flags;
	}
	
	public void setFlags(byte flags) {
		this.flags = flags;
	}
	
	public short getRcvWindow() {
		return rcvWindow;
	}
	
	public void setRcvWindow(short rcvWindow) {
		this.rcvWindow = rcvWindow;
	}
	
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	
}
