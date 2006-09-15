/*
 * OPMLWriter.java
 *
 * Created on September 15, 2006, 7:26 AM
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
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author davidg
 */
public class OPMLWriter implements IdeaWriter {
    private Writer out;
    
    public OPMLWriter(Writer out) {
        this.out = out;
    }
    
    public void write(Idea idea) throws IOException {
        save(idea);
        out.flush();
        out.close();
    }
    
    private void save(Idea idea) throws IOException {
        out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
        out.write("<opml version=\"1.0\">\n");
        
        out.write("     <head>\n");
        out.write("          <title/>\n");
        out.write("     </head>\n");
        
        out.write("     <body>\n");
        
        saveIdea(idea);
        
        out.write("     </body>\n");
        out.write("</opml>\n");
    }
    
    private void saveIdea(Idea idea) throws IOException {
        out.write("<outline text=\"" + idea.getText() + "\">\n");
        for (Idea subIdea: idea.getSubIdeas()) {
            saveIdea(subIdea);
        }
        out.write("</outline>\n");
    }
}
