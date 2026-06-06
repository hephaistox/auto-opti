#include <stdint.h>
#include <stdio.h>

// Canonical xoshiro256+ reference, https://prng.di.unimi.it/xoshiro256plus.c
static inline uint64_t rotl(const uint64_t x, int k) {
   return (x << k) | (x >> (64 - k));
}

static uint64_t s[4];

uint64_t next(void) {
   const uint64_t result = s[0] + s[3];
   const uint64_t t = s[1] << 17;

   s[2] ^= s[0];
   s[3] ^= s[1];
   s[1] ^= s[2];
   s[0] ^= s[3];
   s[2] ^= t;
   s[3] = rotl(s[3], 45);

   return result;
}

int main(void) {
   // Initialize with known state
   s[0] = 1;
   s[1] = 2;
   s[2] = 3;
   s[3] = 4;

   for(int i = 0; i < 20; i++) {
       printf("%llu\n", next());
   }
   return 0;
}
