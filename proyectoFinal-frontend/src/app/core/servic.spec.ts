import { TestBed } from '@angular/core/testing';

import { Servic } from './servic';

describe('Servic', () => {
  let service: Servic;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Servic);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
