package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.OEntityRef;
import org.odata4j.internal.EntitySegment;
import org.odata4j.internal.InternalUtil;
import org.odata4j.xml.AtomFeedParser.AtomEntry;
import org.odata4j.xml.AtomFeedParser.DataServicesAtomEntry;

import core4j.Enumerable;

public class OEntityRefImpl<T> implements OEntityRef<T> {

    private final boolean isDelete;
    private final ODataClient client;
    private final String serviceRootUri;
    private final List<EntitySegment> segments = new ArrayList<EntitySegment>();

    public OEntityRefImpl(boolean isDelete, ODataClient client, String serviceRootUri, String entitySetName, Object[] key) {
        this.isDelete = isDelete;
        this.client = client;
        this.serviceRootUri = serviceRootUri;

        segments.add(new EntitySegment(entitySetName, key));

    }

    @Override
    public OEntityRef<T> nav(String navProperty, Object... key) {
        segments.add(new EntitySegment(navProperty, key));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T execute() {

        String path = Enumerable.create(segments).join("/");

        if (isDelete) {
            ODataClientRequest request = ODataClientRequest.delete(serviceRootUri + path);
            client.deleteEntity(request);
            return null;

        } else {

            ODataClientRequest request = ODataClientRequest.get(serviceRootUri + path);

            AtomEntry entry = client.getEntity(request);
            if (entry == null)
                return null;
            DataServicesAtomEntry dsae = (DataServicesAtomEntry) entry;

            return (T) InternalUtil.toEntity(dsae);
        }
    }

}