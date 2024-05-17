import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from 'src/app/services/services';

@Component({
  selector: 'app-activate-account',
  templateUrl: './activate-account.component.html',
  styleUrls: ['./activate-account.component.scss']
})
export class ActivateAccountComponent implements OnInit {

  message: string = '';
  isOkay: boolean = true;
  submitted: boolean = false;

  constructor(private router: Router, private authService: AuthenticationService) { }

  ngOnInit(): void { }

  onCodeCompleted(token: string): void {
    this.confirmAccount(token);
  }

  redirectToLogin(): void {
    this.router.navigate(['login']);
  }

  private confirmAccount(token: string): void {
    this.authService.confirm({ token }).subscribe({
      next: () => {
        this.message = 'Your account has been successfully activated. Now you can proceed to login';
        this.submitted = true;
        this.isOkay = true;
      },
      error: () => {
        this.message = 'Token has expired or is invalid';
        this.submitted = false;
        this.isOkay = false;
      }
    });
  }
}
