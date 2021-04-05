public class Triplet<V1, V2, V3> {
    private final V1 v1;
    private final V2 v2;
    private final V3 v3;

    public Triplet(V1 v1, V2 v2, V3 v3) { this.v1 = v1; this.v2 = v2; this.v3 = v3; }

    public V1 getV1() { return v1; }
    public V2 getV2() { return v2; }
    public V3 getV3() { return v3; }

    @Override public String toString() { return "(" + v1 + ", " + v2 + ", " + v3 + ")"; }
}
