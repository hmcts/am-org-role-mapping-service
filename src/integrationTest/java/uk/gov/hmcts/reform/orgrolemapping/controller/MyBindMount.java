package uk.gov.hmcts.reform.orgrolemapping.controller;

import java.util.Objects;

import org.testcontainers.containers.BindMode;

public final class MyBindMount {
    public static MyBindMount of(String localFile, String remoteFile, BindMode bindMode) {
        return new MyBindMount(localFile, remoteFile, bindMode);
    }

    private final String localFile;
    private final String remoteFile;
    private final BindMode bindMode;

    private MyBindMount(String localFile, String remoteFile, BindMode bindMode) {
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.bindMode = bindMode == null ? BindMode.READ_ONLY : bindMode;
    }

    public BindMode getBindMode() {
        return bindMode;
    }

    public String getLocalFile() {
        return localFile;
    }

    public String getRemoteFile() {
        return remoteFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)  {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MyBindMount bindMount = (MyBindMount) o;
        return Objects.equals(localFile, bindMount.localFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localFile);
    }

    @Override
    public String toString() {
        return "BindMount{"
                + "localFile='" + localFile + '\''
                + ", remoteFile='" + remoteFile + '\''
                + ", bindMode=" + bindMode
                + '}';
    }
}
