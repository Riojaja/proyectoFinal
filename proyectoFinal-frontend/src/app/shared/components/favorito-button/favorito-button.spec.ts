import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FavoritoButton } from './favorito-button';

describe('FavoritoButton', () => {
  let component: FavoritoButton;
  let fixture: ComponentFixture<FavoritoButton>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FavoritoButton]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FavoritoButton);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
