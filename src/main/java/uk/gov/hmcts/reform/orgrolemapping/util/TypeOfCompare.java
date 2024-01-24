package uk.gov.hmcts.reform.orgrolemapping.util;

public enum TypeOfCompare {
    //performing sequential operations on data from collections, which includes filtering
    // the differences between lists
    //repeated calling of List.contains() can be a costly operation for larger lists
    STREAM,

    //converting the List to a Set will have the effect of duplicating and reordering it
    SET
}
