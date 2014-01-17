/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sica.ssh;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author ingenieria4
 */
public class ScpInputStream extends InputStream {

    long length;
    InputStream in;
    long count;
    ScpChannel channel;

    ScpInputStream(long length, InputStream in, ScpChannel channel) {
        this.length = length;
        this.in = in;
        this.channel = channel;
    }

    public int read() throws IOException {
        if (count == length) {
            return -1;
        }
        if (count >= length) {
            throw new EOFException("End of file.");
        }
        int r = in.read();
        if (r == -1) {
            throw new EOFException("Unexpected EOF.");
        }
        count++;
        if (count == length) {
            
            channel.waitForResponse();
            channel.writeOk();
        }
        return r;
    }

    public void close() throws IOException {
        channel.close();
    }
}
