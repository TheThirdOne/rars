package rars.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

	/*
Copyright (c) 2003-2006,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * Provides means to work with ".properties" files which are used to store
 * various RARS settings.
 *
 * @author Pete Sanderson
 * @version October 2006
 */
public class PropertiesFile {

    /**
     * Produce Properties (a Hashtable) object containing key-value pairs
     * from specified properties file.  This may be used as an alternative
     * to readPropertiesFile() which uses a different implementation.
     *
     * @param file Properties filename.  Do NOT include the file extension as
     *             it is assumed to be ".properties" and is added here.
     * @return Properties (Hashtable) of key-value pairs read from the file.
     */

    public static Properties loadPropertiesFromFile(String file) {
        Properties properties = new Properties();
        try {
            InputStream is = PropertiesFile.class.getResourceAsStream("/" + file + ".properties");
            properties.load(is);
        } catch (IOException ioe) {
        } // If it doesn't work, properties will be empty
        catch (NullPointerException npe) {
        }
        return properties;
    }
}

