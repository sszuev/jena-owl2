package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;

class TestModelFactory {
    static final String NS = "http://example.com/test#";

    static OntModel createClassesABCD(OntModel m) {
        //    A
        //  / |
        // B  C
        //     \
        //      D
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        A.addSubClass(B);
        A.addSubClass(C);
        C.addSubClass(D);
        return m;
    }

    static OntModel createClassesABCDEFGHKLM(OntModel m) {
        //      A
        //   / |  \
        //  /  B   C
        //  | / \ / \
        //  D    E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        OntClass E = m.createOntClass(NS + "E");
        OntClass F = m.createOntClass(NS + "F");
        OntClass G = m.createOntClass(NS + "G");
        OntClass H = m.createOntClass(NS + "H");
        OntClass K = m.createOntClass(NS + "K");
        OntClass L = m.createOntClass(NS + "L");
        OntClass M = m.createOntClass(NS + "M");

        A.addSubClass(D);
        A.addSubClass(B);
        A.addSubClass(C);

        B.addSubClass(D);
        B.addSubClass(E);

        C.addSubClass(E);
        C.addSubClass(F);

        D.addSubClass(G);
        D.addSubClass(H);

        H.addSubClass(K);

        K.addSubClass(H);
        K.addSubClass(L);
        K.addSubClass(M);
        return m;
    }

    static OntModel createClassesABCDEF(OntModel m) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        OntClass E = m.createOntClass(NS + "E");
        OntClass F = m.createOntClass(NS + "F");

        A.addSubClass(B);
        A.addSubClass(C);
        B.addSubClass(D);
        B.addSubClass(E);
        C.addSubClass(E);
        C.addSubClass(F);
        return m;
    }

    static OntModel createClassesDBCA(OntModel m) {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A

        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");

        C.addSubClass(A);
        B.addSubClass(A);
        D.addSubClass(C);
        D.addSubClass(A);
        return m;
    }

    static OntModel createClassesBCA(OntModel m) {
        // B = C
        //  \ |
        //    A

        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");

        A.addSuperClass(B);
        A.addSuperClass(C);
        B.addSuperClass(C);
        C.addSuperClass(B);
        return m;
    }

    static OntModel createClassesDGCFKBEHAG(OntModel m) {
        // D        G
        // |      / .
        // C  F  K  .
        // |  |  |  .
        // B  E  H  .
        // |      \ .
        // A        G

        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        OntClass E = m.createOntClass(NS + "E");
        OntClass F = m.createOntClass(NS + "F");
        OntClass G = m.createOntClass(NS + "G");
        OntClass H = m.createOntClass(NS + "H");
        OntClass K = m.createOntClass(NS + "K");

        A.addSuperClass(B);
        B.addSuperClass(C);
        C.addSuperClass(D);
        E.addSuperClass(F);
        G.addSuperClass(H);
        H.addSuperClass(K);
        K.addSuperClass(G);
        return m;
    }

    static OntModel createClassesABCA(OntModel m) {
        //    A
        //  / .
        // B  .
        // |  .
        // C  .
        //  \ .
        //    A

        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");

        A.addSubClass(B);
        B.addSubClass(C);
        C.addSubClass(A);
        return m;
    }

    static OntModel createClassesDBFCEA(OntModel m) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A

        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        OntClass E = m.createOntClass(NS + "E");
        OntClass F = m.createOntClass(NS + "F");
        D.addSubClass(F);
        D.addSubClass(B);
        B.addSubClass(C);
        F.addSubClass(E);
        E.addSubClass(A);
        C.addSubClass(A);
        return m;
    }
}
