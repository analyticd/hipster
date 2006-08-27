/*
 * OPMLReader.java
 *
 * Created on August 27, 2006, 11:26 AM
 *
 * Copyright (c) 2006, David Griffiths
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this Vector of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this Vector of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the David Griffiths nor the names of his contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package dg.hipster.io;

import dg.hipster.model.Idea;
import java.io.InputStream;
import java.util.Stack;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author davidg
 */
public class OPMLReader extends DefaultHandler implements IdeaReader {
    private Idea idea;
    private Idea current;
    private Stack<Idea> stack = new Stack<Idea>();
    public void startElement(String namespaceURI,
            String sName, // simple name (localName)
            String qName, // qualified name
            Attributes attrs)
            throws SAXException {
        System.out.println("qName = " + qName);
        if ("outline".equals(qName)) {
            if (attrs != null) {
                String text = attrs.getValue("text");
                System.out.println("text = " + text);
                Idea i = new Idea(text);
                if (idea == null) {
                    idea = i;
                    System.out.println("idea = " + idea);
                } else {
                    System.out.println("adding " + i + " to " + current);
                    current.add(i);
                }
                current = i;
                stack.push(current);
            }
        }
    }
    public void endElement(String namespaceURI,
            String sName, // simple name
            String qName  // qualified name
            )
            throws SAXException {
        if ("outline".equals(qName)) {
            stack.pop();
            if (stack.isEmpty()) {
                current = null;
            } else {
                current = stack.peek();
            }
        }
    }
    public OPMLReader(InputStream in) throws ReaderException {
        try {
            SAXParser p = SAXParserFactory.newInstance().newSAXParser();
            p.parse(in, this);
        } catch(Exception e) {
            throw new ReaderException("Unable to read OPML", e);
        }
    }
    public Idea getIdea() {
        return idea;
    }
}
