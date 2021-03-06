/*-
 * ========================LICENSE_START=================================
 * restheart-mongodb
 * %%
 * Copyright (C) 2014 - 2020 SoftInstigate
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =========================LICENSE_END==================================
 */
package org.restheart.mongodb.handlers.bulk;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import java.util.List;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.restheart.mongodb.db.BulkOperationResult;
import org.restheart.mongodb.utils.ResponseHelper;
import org.restheart.representation.IllegalQueryParamenterException;
import org.restheart.representation.RepresentationUtils;
import org.restheart.utils.HttpStatus;

/**
 *
 * @author Andrea Di Cesare {@literal <andrea@softinstigate.com>}
 */
class BulkResultRepresentationFactory {

    /**
     *
     */
    public BulkResultRepresentationFactory() {
    }

    /**
     *
     * @param exchange
     * @param result
     * @return
     * @throws IllegalQueryParamenterException
     */
    public BsonDocument getRepresentation(String requestPath, BulkOperationResult result)
            throws IllegalQueryParamenterException {
        var rep = new BsonDocument();

        addBulkResult(result, requestPath, rep);

        return rep;
    }

    /**
     *
     * @param exchange
     * @param mbwe
     * @return
     * @throws IllegalQueryParamenterException
     */
    public BsonDocument getRepresentation(String requestPath, MongoBulkWriteException mbwe)
            throws IllegalQueryParamenterException {

        var rep = new BsonDocument();

        addWriteResult(mbwe.getWriteResult(), rep, requestPath);

        addWriteErrors(mbwe.getWriteErrors(), rep);

        return rep;
    }

    private void addBulkResult(
            final BulkOperationResult result,
            final String requestPath,
            final BsonDocument rep) {
        BulkWriteResult wr = result.getBulkResult();

        if (wr.wasAcknowledged()) {
            if (wr.getUpserts() != null) {
                rep.put("inserted",
                        new BsonInt32(wr.getUpserts().size()));

                var links = new BsonArray();

                // add links to new, upserted documents
                wr.getUpserts().stream().
                        forEach(update -> {
                            links.add(new BsonString(
                                    RepresentationUtils
                                            .getReferenceLink(
                                                    requestPath,
                                                    update.getId())));
                        });
                
                rep.put("links", links);
            }

            rep.put("deleted",
                    new BsonInt32(wr.getDeletedCount()));

            rep.put("modified",
                    new BsonInt32(wr.getModifiedCount()));

            rep.put("matched",
                    new BsonInt32(wr.getMatchedCount()));
        }
    }

    private void addWriteResult(
            final BulkWriteResult wr,
            final BsonDocument rep,
            final String requestPath) {
        if (wr.wasAcknowledged()) {
            if (wr.getUpserts() != null) {
                rep.put("inserted",
                        new BsonInt32(wr.getUpserts().size()));

                var links = new BsonArray();

                // add links to new, upserted documents
                wr.getUpserts().stream().
                        forEach(update -> {
                            links.add(new BsonString(
                                    RepresentationUtils
                                            .getReferenceLink(
                                                    requestPath,
                                                    update.getId())));
                        });
                
                rep.put("links", links);
            }

            rep.put("deleted",
                    new BsonInt32(wr.getDeletedCount()));

            rep.put("modified",
                    new BsonInt32(wr.getModifiedCount()));

            rep.put("matched",
                    new BsonInt32(wr.getMatchedCount()));
        }
    }

    private void addWriteErrors(
            final List<BulkWriteError> wes,
            final BsonDocument rep) {
        wes.stream().forEach(error -> {
            // error 11000 is duplicate key error
            // happens when the _id and a filter are specified,
            // the document exists but does not match the filter
            if (error.getCode() == 11000
                    && error.getMessage().contains("_id_ dup key")) {
                rep.put("index",
                        new BsonInt32(error.getIndex()));
                rep.put("httpStatus",
                        new BsonInt32(
                                ResponseHelper.getHttpStatusFromErrorCode(
                                        error.getCode())));
            } else if (error.getCode() == 2) {
                rep.put("index",
                        new BsonInt32(error.getIndex()));
                rep.put("httpStatus",
                        new BsonInt32(
                                ResponseHelper.getHttpStatusFromErrorCode(
                                        error.getCode())));
                rep.put("message",
                        new BsonString(
                                ResponseHelper.getMessageFromErrorCode(
                                        error.getCode())
                                + ": "
                                + error.getMessage()));
            } else {
                rep.put("index",
                        new BsonInt32(error.getIndex()));
                rep.put("mongodbErrorCode",
                        new BsonInt32(error.getCode()));
                rep.put("httpStatus",
                        new BsonInt32(HttpStatus.SC_NOT_FOUND));
                rep.put("message",
                        new BsonString(
                                ResponseHelper.getMessageFromErrorCode(
                                        error.getCode())));
            }
        });
    }
}
