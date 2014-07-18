/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2013 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.mongodb.xquery.gridfs;

import com.mongodb.MongoClient;
import org.exist.dom.QName;
import org.exist.mongodb.shared.Constants;
import org.exist.mongodb.shared.MongodbClientStore;
import org.exist.mongodb.xquery.GridfsModule;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.*;

/**
 * Implementation gridfs:close() functions
 *
 * @author Dannes Wessels
 */
public class Close extends BasicFunction {

    public final static FunctionSignature signatures[] = {
        new FunctionSignature(
        new QName("close", GridfsModule.NAMESPACE_URI, GridfsModule.PREFIX),
        "Close MongoDB connector",
        new SequenceType[]{
            new FunctionParameterSequenceType("token", Type.STRING, Cardinality.ONE, "token")
        },
        new FunctionReturnSequenceType(Type.EMPTY, Cardinality.ZERO, "none")
        ),};

    public Close(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {

        // User must either be DBA or in the JMS group
        if (!context.getSubject().hasDbaRole() && !context.getSubject().hasGroup(Constants.MONGODB_GROUP)) {
            String txt = String.format("Permission denied, user '%s' must be a DBA or be in group '%s'",
                    context.getSubject().getName(), Constants.MONGODB_GROUP);
            XPathException ex = new XPathException(this, txt);
            LOG.error(txt, ex);
            throw ex;
        }

        // Get connection URL
        String driverId = args[0].itemAt(0).getStringValue();

        // Handle ()
        try {
            MongoClient client = MongodbClientStore.getInstance().get(driverId);

            if (client == null) {
                throw new XPathException(this, String.format("Mongoclient %s could not be found.", driverId));
            }

            // CLose connector with all connections
            client.close();
            
            // Remove from cache
            MongodbClientStore.getInstance().remove(driverId);

            // Report identifier
            return EmptySequence.EMPTY_SEQUENCE;

        } catch (Exception t) {
            LOG.error(t.getMessage(), t);
            XPathException ex = new XPathException(this, t);
            throw ex;
        }

    }
}