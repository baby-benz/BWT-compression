package struct;

public class SuffixArray {
    private interface BaseArray {
        int get(int i);

        void set(int i, int val);

        int update(int i, int val);
    }

    public static final class ByteArray implements BaseArray {
        private final byte[] mA;
        private final int mPos;

        public ByteArray(byte[] A, int pos) {
            mA = A;
            mPos = pos;
        }

        @Override
        public int get(int i) {
            return mA[mPos + i] & 0xFF;
        }

        @Override
        public void set(int i, int val) {
            mA[mPos + i] = (byte) (val & 0xFF);
        }

        @Override
        public int update(int i, int val) {
            return mA[mPos + i] += val & 0xFF;
        }
    }

    public static final class IntArray implements BaseArray {
        private final int[] mA;
        private final int mPos;

        public IntArray(int[] A, int pos) {
            mA = A;
            mPos = pos;
        }

        @Override
        public int get(int i) {
            return mA[mPos + i];
        }

        @Override
        public void set(int i, int val) {
            mA[mPos + i] = val;
        }

        @Override
        public int update(int i, int val) {
            return mA[mPos + i] += val;
        }
    }

    private static void getCounts(BaseArray T, BaseArray C, int n, int k) {
        for (int i = 0; i < k; ++i) {
            C.set(i, 0);
        }
        for (int i = 0; i < n; ++i) {
            C.update(T.get(i), 1);
        }
    }

    private static void getBuckets(BaseArray C, BaseArray B, int k, boolean end) {
        int i, sum = 0;
        if (end) {
            for (i = 0; i < k; ++i) {
                sum += C.get(i);
                B.set(i, sum);
            }
        } else {
            for (i = 0; i < k; ++i) {
                sum += C.get(i);
                B.set(i, sum - C.get(i));
            }
        }
    }

    private static void induceSA(BaseArray T, int[] SA, BaseArray C, BaseArray B, int n, int k) {
        int b, i, j;
        int c0, c1;

        if (C == B) {
            getCounts(T, C, n, k);
        }

        getBuckets(C, B, k, false);
        j = n - 1;
        b = B.get(c1 = T.get(j));
        SA[b++] = ((0 < j) && (T.get(j - 1) < c1)) ? ~j : j;

        for (i = 0; i < n; ++i) {
            j = SA[i];
            SA[i] = ~j;
            if (0 < j) {
                if ((c0 = T.get(--j)) != c1) {
                    B.set(c1, b);
                    b = B.get(c1 = c0);
                }
                SA[b++] = ((0 < j) && (T.get(j - 1) < c1)) ? ~j : j;
            }
        }

        if (C == B) {
            getCounts(T, C, n, k);
        }
        getBuckets(C, B, k, true);

        for (i = n - 1, b = B.get(c1 = 0); 0 <= i; --i) {
            if (0 < (j = SA[i])) {
                if ((c0 = T.get(--j)) != c1) {
                    B.set(c1, b);
                    b = B.get(c1 = c0);
                }
                SA[--b] = ((j == 0) || (T.get(j - 1) > c1)) ? ~j : j;
            } else {
                SA[i] = ~j;
            }
        }
    }

    private static int computeBWT(BaseArray T, int[] SA, BaseArray C, BaseArray B, int n, int k) {
        int b, i, j, pidx = -1;
        int c0, c1;

        if (C == B) {
            getCounts(T, C, n, k);
        }

        getBuckets(C, B, k, false);
        j = n - 1;
        b = B.get(c1 = T.get(j));
        SA[b++] = ((0 < j) && (T.get(j - 1) < c1)) ? ~j : j;

        for (i = 0; i < n; ++i) {
            if (0 < (j = SA[i])) {
                SA[i] = ~(c0 = T.get(--j));
                if (c0 != c1) {
                    B.set(c1, b);
                    b = B.get(c1 = c0);
                }
                SA[b++] = ((0 < j) && (T.get(j - 1) < c1)) ? ~j : j;
            } else if (j != 0) {
                SA[i] = ~j;
            }
        }

        if (C == B) {
            getCounts(T, C, n, k);
        }

        getBuckets(C, B, k, true);

        for (i = n - 1, b = B.get(c1 = 0); 0 <= i; --i) {
            if (0 < (j = SA[i])) {
                SA[i] = (c0 = T.get(--j));
                if (c0 != c1) {
                    B.set(c1, b);
                    b = B.get(c1 = c0);
                }
                SA[--b] = ((0 < j) && (T.get(j - 1) > c1)) ? ~((int) T.get(j - 1)) : j;
            } else if (j != 0) {
                SA[i] = ~j;
            } else {
                pidx = i;
            }
        }

        return pidx;
    }

    public static int SA_IS(BaseArray T, int[] SA, int fs, int n, int k, boolean isBwt) {
        BaseArray C, B, RA;
        int i, j, c, m, p, q, plen, qlen, name, pidx = 0;
        int c0, c1;
        boolean diff;

        if (k <= fs) {
            C = new IntArray(SA, n);
            B = (k <= (fs - k)) ? new IntArray(SA, n + k) : C;
        } else {
            B = C = new IntArray(new int[k], 0);
        }

        getCounts(T, C, n, k);
        getBuckets(C, B, k, true);

        for (i = 0; i < n; ++i) {
            SA[i] = 0;
        }

        for (i = n - 2, c = 0, c1 = T.get(n - 1); 0 <= i; --i, c1 = c0) {
            if ((c0 = T.get(i)) < (c1 + c)) {
                c = 1;
            } else if (c != 0) {
                SA[B.update(c1, -1)] = i + 1;
                c = 0;
            }
        }

        induceSA(T, SA, C, B, n, k);

        for (i = 0, m = 0; i < n; ++i) {
            p = SA[i];
            if ((0 < p) && (T.get(p - 1) > (c0 = T.get(p)))) {
                j = p + 1;
                while ((j < n) && (c0 == (c1 = T.get(j)))) {
                    ++j;
                }
                if ((j < n) && (c0 < c1)) {
                    SA[m++] = p;
                }
            }
        }

        j = m + (n >> 1);

        for (i = m; i < j; ++i) {
            SA[i] = 0;
        }

        for (i = n - 2, j = n, c = 0, c1 = T.get(n - 1); 0 <= i; --i, c1 = c0) {
            if ((c0 = T.get(i)) < (c1 + c)) {
                c = 1;
            } else if (c != 0) {
                SA[m + ((i + 1) >> 1)] = j - i - 1;
                j = i + 1;
                c = 0;
            }
        }

        for (i = 0, name = 0, q = n, qlen = 0; i < m; ++i) {
            p = SA[i];
            plen = SA[m + (p >> 1)];
            diff = true;
            if (plen == qlen) {
                j = 0;
                while ((j < plen) && (T.get(p + j) == T.get(q + j))) {
                    ++j;
                }
                if (j == plen) {
                    diff = false;
                }
            }
            if (diff) {
                ++name;
                q = p;
                qlen = plen;
            }
            SA[m + (p >> 1)] = name;
        }

        if (name < m) {
            RA = new IntArray(SA, n + fs - m);
            for (i = m + (n >> 1) - 1, j = n + fs - 1; m <= i; --i) {
                if (SA[i] != 0) {
                    SA[j--] = SA[i] - 1;
                }
            }
            SA_IS(RA, SA, fs + n - m * 2, m, name, false);
            for (i = n - 2, j = m * 2 - 1, c = 0, c1 = T.get(n - 1); 0 <= i; --i, c1 = c0) {
                if ((c0 = T.get(i)) < (c1 + c)) {
                    c = 1;
                } else if (c != 0) {
                    SA[j--] = i + 1;
                    c = 0;
                }
            }
            for (i = 0; i < m; ++i) {
                SA[i] = SA[SA[i] + m];
            }
        }

        if (k <= fs) {
            C = new IntArray(SA, n);
            B = (k <= (fs - k)) ? new IntArray(SA, n + k) : C;
        } else {
            B = C = new IntArray(new int[k], 0);
        }

        getCounts(T, C, n, k);
        getBuckets(C, B, k, true);

        for (i = m; i < n; ++i) {
            SA[i] = 0;
        }

        for (i = m - 1; 0 <= i; --i) {
            j = SA[i];
            SA[i] = 0;
            SA[B.update(T.get(j), -1)] = j;
        }

        if (!isBwt) {
            induceSA(T, SA, C, B, n, k);
        } else {
            pidx = computeBWT(T, SA, C, B, n, k);
        }

        return pidx;
    }
}
