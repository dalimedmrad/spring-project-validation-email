import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RegistrationRequest } from 'src/app/services/models';
import { AuthenticationService } from 'src/app/services/services';
import { TokenService } from 'src/app/services/token/token.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

  constructor(private router: Router,
    private authService: AuthenticationService,
    private tokenService: TokenService) { }

  registerRequest: RegistrationRequest = { email: '', firstname: '', lastname: '', password: '' };
  errorMsg: Array<string> = [];

  register() { 
    this.errorMsg=[];
    this.authService.register({
      body:this.registerRequest
    }).subscribe({
      next:()=>{
        this.router.navigate(['activate-account']);
      },
      error:(err)=>{
        this.errorMsg= err.error.validationErrors;
      }
    })
  }
  login() {
    this.router.navigate(['login'])

  }

  ngOnInit(): void {
  }

}
