import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {CustomerListComponent} from './customer/customer-list.component/customer-list.component';
import {HomeComponent} from './home.component/home.component';

export const routes: Routes = [
  {path: 'home', component: HomeComponent},
  {path: "customer", component: CustomerListComponent},
  ];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
